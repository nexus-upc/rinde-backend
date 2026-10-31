package com.nexus.rinde.iam.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexus.rinde.iam.application.internal.TokenValidityProperties;
import com.nexus.rinde.iam.application.internal.outboundservices.AuthenticationTokenService;
import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.domain.model.commands.DefinePasswordCommand;
import com.nexus.rinde.iam.domain.model.commands.RequestPasswordResetCommand;
import com.nexus.rinde.iam.domain.model.commands.SignInCommand;
import com.nexus.rinde.iam.domain.model.events.PasswordResetRequested;
import com.nexus.rinde.iam.domain.model.events.UserInvited;
import com.nexus.rinde.iam.domain.model.valueobjects.AccessToken;
import com.nexus.rinde.iam.domain.model.valueobjects.AuthenticationResult;
import com.nexus.rinde.iam.domain.model.valueobjects.Email;
import com.nexus.rinde.iam.domain.model.valueobjects.PasswordHash;
import com.nexus.rinde.iam.domain.model.valueobjects.Role;
import com.nexus.rinde.iam.domain.model.valueobjects.Ruc;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantStatus;
import com.nexus.rinde.iam.domain.model.valueobjects.TokenPurpose;
import com.nexus.rinde.iam.domain.model.valueobjects.UserStatus;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.TenantRepository;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.nexus.rinde.shared.domain.exceptions.AuthenticationFailedException;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import com.nexus.rinde.shared.domain.exceptions.ForbiddenOperationException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationCommandServiceImplTest {

  private static final Instant NOW = Instant.parse("2026-10-06T10:00:00Z");
  private static final String PASSWORD = "Clave-Segura-2026";

  @Mock private UserRepository userRepository;
  @Mock private TenantRepository tenantRepository;
  @Mock private AuthenticationTokenService tokenService;

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
  private AuthenticationCommandServiceImpl service;

  private Tenant tenant;
  private User user;

  @BeforeEach
  void setUp() {
    service =
        new AuthenticationCommandServiceImpl(
            userRepository,
            tenantRepository,
            passwordEncoder,
            tokenService,
            new TokenValidityProperties(48, 1, 48),
            Clock.fixed(NOW, ZoneOffset.UTC));
    tenant = new Tenant("Transportes Andes", new Ruc("20123456789"));
    tenant.register(
        new Email("ana@andes.pe"),
        AccessToken.issue(
            tenant.getTenantId(), TokenPurpose.EMAIL_VERIFICATION, Duration.ofHours(48), NOW),
        NOW);
    user =
        User.createAdministrator(
            tenant.getTenantId(),
            "Ana Rojas",
            new Email("ana@andes.pe"),
            new PasswordHash(passwordEncoder.encode(PASSWORD)));
  }

  private void givenUserAndTenantExist() {
    when(userRepository.findByEmail(new Email("ana@andes.pe"))).thenReturn(Optional.of(user));
    when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
  }

  @Test
  void signInIssuesATokenWithTheTenantStatus() {
    tenant.verify();
    givenUserAndTenantExist();
    AuthenticationResult expected = new AuthenticationResult("jwt", 28800);
    when(tokenService.issueFor(user, TenantStatus.ACTIVE)).thenReturn(expected);

    AuthenticationResult result = service.handle(new SignInCommand("Ana@Andes.pe", PASSWORD));

    assertThat(result).isSameAs(expected);
  }

  @Test
  void signInIsAllowedForARestrictedTenantAndCarriesItsStatus() {
    tenant.verify();
    tenant.restrict();
    givenUserAndTenantExist();
    when(tokenService.issueFor(user, TenantStatus.RESTRICTED))
        .thenReturn(new AuthenticationResult("jwt", 1));

    assertThat(service.handle(new SignInCommand("ana@andes.pe", PASSWORD)).accessToken())
        .isEqualTo("jwt");
  }

  @Test
  void signInIsForbiddenWhileTheTenantIsPendingVerification() {
    givenUserAndTenantExist();

    assertThatThrownBy(() -> service.handle(new SignInCommand("ana@andes.pe", PASSWORD)))
        .isInstanceOf(ForbiddenOperationException.class)
        .hasMessageContaining("confirmó su correo");
    verify(tokenService, never()).issueFor(any(), any());
  }

  @Test
  void signInFailsWithAWrongPassword() {
    tenant.verify();
    when(userRepository.findByEmail(new Email("ana@andes.pe"))).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> service.handle(new SignInCommand("ana@andes.pe", "otra-clave")))
        .isInstanceOf(AuthenticationFailedException.class);
  }

  @Test
  void signInFailsWithAnUnknownEmailWithTheSameMessage() {
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.handle(new SignInCommand("nadie@andes.pe", PASSWORD)))
        .isInstanceOf(AuthenticationFailedException.class)
        .hasMessage("El correo o la contraseña son incorrectos.");
  }

  @Test
  void signInTreatsAnInvalidEmailAsUnknown() {
    assertThatThrownBy(() -> service.handle(new SignInCommand("no-es-correo", PASSWORD)))
        .isInstanceOf(AuthenticationFailedException.class);
  }

  @Test
  void signInFailsForAnInvitedUserWithoutPassword() {
    User invited = new User(tenant.getTenantId(), "Luis", new Email("luis@andes.pe"), Role.DRIVER);
    invited.invite(Duration.ofHours(48), NOW);
    when(userRepository.findByEmail(new Email("luis@andes.pe"))).thenReturn(Optional.of(invited));

    assertThatThrownBy(() -> service.handle(new SignInCommand("luis@andes.pe", PASSWORD)))
        .isInstanceOf(AuthenticationFailedException.class);
  }

  @Test
  void signInIsForbiddenForADisabledUser() {
    user.disable();
    when(userRepository.findByEmail(new Email("ana@andes.pe"))).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> service.handle(new SignInCommand("ana@andes.pe", PASSWORD)))
        .isInstanceOf(ForbiddenOperationException.class)
        .hasMessageContaining("deshabilitado");
  }

  @Test
  void requestPasswordResetIssuesALinkForAnActiveUser() {
    when(userRepository.findByEmail(new Email("ana@andes.pe"))).thenReturn(Optional.of(user));

    service.handle(new RequestPasswordResetCommand("ana@andes.pe"));

    verify(userRepository).save(user);
    assertThat(user.pendingDomainEvents()).hasAtLeastOneElementOfType(PasswordResetRequested.class);
    assertThat(user.getAccessTokens().get(0).purpose()).isEqualTo(TokenPurpose.PASSWORD_RESET);
    assertThat(user.getAccessTokens().get(0).expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(1)));
  }

  @Test
  void requestPasswordResetDoesNothingForUnknownInvitedOrInvalidEmails() {
    User invited = new User(tenant.getTenantId(), "Luis", new Email("luis@andes.pe"), Role.DRIVER);
    invited.invite(Duration.ofHours(48), NOW);
    when(userRepository.findByEmail(new Email("luis@andes.pe"))).thenReturn(Optional.of(invited));
    when(userRepository.findByEmail(new Email("nadie@andes.pe"))).thenReturn(Optional.empty());

    assertThatCode(
            () -> {
              service.handle(new RequestPasswordResetCommand("luis@andes.pe"));
              service.handle(new RequestPasswordResetCommand("nadie@andes.pe"));
              service.handle(new RequestPasswordResetCommand("no-es-correo"));
            })
        .doesNotThrowAnyException();
    verify(userRepository, never()).save(any());
  }

  @Test
  void definePasswordActivatesAnInvitedUser() {
    User invited = new User(tenant.getTenantId(), "Luis", new Email("luis@andes.pe"), Role.DRIVER);
    invited.invite(Duration.ofHours(48), NOW.minusSeconds(60));
    String token = ((UserInvited) invited.pendingDomainEvents().get(0)).invitationToken();
    when(userRepository.findByAccessTokenHash(AccessToken.hash(token)))
        .thenReturn(Optional.of(invited));

    service.handle(new DefinePasswordCommand(token, "Otra-Clave-2026"));

    assertThat(invited.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(passwordEncoder.matches("Otra-Clave-2026", invited.getPasswordHash().value()))
        .isTrue();
    verify(userRepository).save(invited);
  }

  @Test
  void definePasswordRejectsAnUnknownLink() {
    when(userRepository.findByAccessTokenHash(any())).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.handle(new DefinePasswordCommand("desconocido", "Otra-Clave-2026")))
        .isInstanceOf(BusinessRuleException.class)
        .hasMessage(User.INVALID_LINK_MESSAGE);
  }
}
