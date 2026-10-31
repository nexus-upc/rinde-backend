package com.nexus.rinde.iam.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexus.rinde.iam.application.internal.TokenValidityProperties;
import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.domain.model.commands.ReactivateTenantCommand;
import com.nexus.rinde.iam.domain.model.commands.RegisterTenantCommand;
import com.nexus.rinde.iam.domain.model.commands.RestrictTenantCommand;
import com.nexus.rinde.iam.domain.model.commands.VerifyTenantCommand;
import com.nexus.rinde.iam.domain.model.events.TenantRegistered;
import com.nexus.rinde.iam.domain.model.valueobjects.AccessToken;
import com.nexus.rinde.iam.domain.model.valueobjects.Email;
import com.nexus.rinde.iam.domain.model.valueobjects.IssuedAccessToken;
import com.nexus.rinde.iam.domain.model.valueobjects.PasswordHash;
import com.nexus.rinde.iam.domain.model.valueobjects.Role;
import com.nexus.rinde.iam.domain.model.valueobjects.Ruc;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantStatus;
import com.nexus.rinde.iam.domain.model.valueobjects.TokenPurpose;
import com.nexus.rinde.iam.domain.model.valueobjects.UserStatus;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.TenantRepository;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import com.nexus.rinde.shared.domain.exceptions.ConflictException;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class TenantCommandServiceImplTest {

  private static final Instant NOW = Instant.parse("2026-10-06T10:00:00Z");

  @Mock private TenantRepository tenantRepository;
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private TenantCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new TenantCommandServiceImpl(
            tenantRepository,
            userRepository,
            passwordEncoder,
            new TokenValidityProperties(48, 1, 48),
            Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private RegisterTenantCommand registerCommand() {
    return new RegisterTenantCommand(
        "Transportes Andes", "20123456789", "Ana Rojas", "Ana@Andes.pe", "Clave-Segura-2026");
  }

  private Tenant pendingTenant() {
    Tenant tenant = new Tenant("Transportes Andes", new Ruc("20123456789"));
    IssuedAccessToken token =
        AccessToken.issue(
            tenant.getTenantId(), TokenPurpose.EMAIL_VERIFICATION, Duration.ofHours(48), NOW);
    tenant.register(new Email("ana@andes.pe"), token, NOW);
    return tenant;
  }

  private Tenant activeTenant() {
    Tenant tenant = pendingTenant();
    tenant.verify();
    return tenant;
  }

  @Test
  void registersThePendingTenantAndItsActiveAdministrator() {
    when(passwordEncoder.encode("Clave-Segura-2026")).thenReturn("$2a$10$encoded");

    Tenant tenant = service.handle(registerCommand());

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(tenantRepository).saveAndFlush(tenant);
    verify(userRepository).saveAndFlush(userCaptor.capture());
    User administrator = userCaptor.getValue();
    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.PENDING_VERIFICATION);
    assertThat(administrator.getRole()).isEqualTo(Role.ADMINISTRATOR);
    assertThat(administrator.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(administrator.getEmail().address()).isEqualTo("ana@andes.pe");
    assertThat(administrator.getPasswordHash()).isEqualTo(new PasswordHash("$2a$10$encoded"));
    assertThat(administrator.getTenantId()).isEqualTo(tenant.getTenantId());
    assertThat(administrator.getAccessTokens()).hasSize(1);
    assertThat(administrator.getAccessTokens().get(0).purpose())
        .isEqualTo(TokenPurpose.EMAIL_VERIFICATION);
    assertThat(tenant.pendingDomainEvents()).hasAtLeastOneElementOfType(TenantRegistered.class);
  }

  @Test
  void rejectsADuplicatedRuc() {
    when(tenantRepository.existsByRuc(new Ruc("20123456789"))).thenReturn(true);

    assertThatThrownBy(() -> service.handle(registerCommand()))
        .isInstanceOf(ConflictException.class);

    verify(tenantRepository, never()).saveAndFlush(any());
  }

  @Test
  void rejectsADuplicatedAdministratorEmail() {
    when(userRepository.existsByEmail(new Email("ana@andes.pe"))).thenReturn(true);

    assertThatThrownBy(() -> service.handle(registerCommand()))
        .isInstanceOf(ConflictException.class);

    verify(tenantRepository, never()).saveAndFlush(any());
  }

  @Test
  void translatesADatabaseUniqueViolationIntoAConflict() {
    when(passwordEncoder.encode(any())).thenReturn("$2a$10$encoded");
    when(tenantRepository.saveAndFlush(any()))
        .thenThrow(new DataIntegrityViolationException("duplicado"));

    assertThatThrownBy(() -> service.handle(registerCommand()))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void rejectsAnInvalidRucBeforeTouchingTheDatabase() {
    RegisterTenantCommand command =
        new RegisterTenantCommand("Empresa", "123", "Ana", "ana@andes.pe", "Clave-Segura-2026");

    assertThatThrownBy(() -> service.handle(command)).isInstanceOf(BusinessRuleException.class);

    verify(tenantRepository, never()).existsByRuc(any());
  }

  @Test
  void verifyActivatesTheTenantAndConsumesTheLink() {
    Tenant tenant = pendingTenant();
    User administrator =
        User.createAdministrator(
            tenant.getTenantId(), "Ana Rojas", new Email("ana@andes.pe"), new PasswordHash("h"));
    IssuedAccessToken issued =
        administrator.issueAccessToken(TokenPurpose.EMAIL_VERIFICATION, Duration.ofHours(48), NOW);
    when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
    when(userRepository.findByAccessTokenHash(AccessToken.hash(issued.rawValue())))
        .thenReturn(Optional.of(administrator));
    when(tenantRepository.save(tenant)).thenReturn(tenant);

    Tenant verified =
        service.handle(new VerifyTenantCommand(tenant.getTenantId(), issued.rawValue()));

    assertThat(verified.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    assertThat(administrator.getAccessTokens().get(0).used()).isTrue();
    verify(userRepository).save(administrator);
  }

  @Test
  void verifyFailsWhenTheTenantDoesNotExist() {
    TenantId unknown = new TenantId(UUID.randomUUID());
    when(tenantRepository.findById(unknown.value())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.handle(new VerifyTenantCommand(unknown, "token")))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void verifyFailsWithAnUnknownToken() {
    Tenant tenant = pendingTenant();
    when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
    when(userRepository.findByAccessTokenHash(any())).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.handle(new VerifyTenantCommand(tenant.getTenantId(), "desconocido")))
        .isInstanceOf(BusinessRuleException.class);
    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.PENDING_VERIFICATION);
  }

  @Test
  void verifyFailsWhenTheLinkBelongsToAnotherTenant() {
    Tenant tenant = pendingTenant();
    User outsider =
        User.createAdministrator(
            new TenantId(UUID.randomUUID()),
            "Otro",
            new Email("otro@otra.pe"),
            new PasswordHash("h"));
    IssuedAccessToken issued =
        outsider.issueAccessToken(TokenPurpose.EMAIL_VERIFICATION, Duration.ofHours(48), NOW);
    when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
    when(userRepository.findByAccessTokenHash(AccessToken.hash(issued.rawValue())))
        .thenReturn(Optional.of(outsider));

    assertThatThrownBy(
            () -> service.handle(new VerifyTenantCommand(tenant.getTenantId(), issued.rawValue())))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void restrictChangesAnActiveTenant() {
    Tenant tenant = activeTenant();
    when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));

    service.handle(new RestrictTenantCommand(tenant.getTenantId()));

    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.RESTRICTED);
    verify(tenantRepository).save(tenant);
  }

  @Test
  void restrictIsIgnoredWhenTheTenantIsNotActive() {
    Tenant tenant = pendingTenant();
    when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));

    service.handle(new RestrictTenantCommand(tenant.getTenantId()));

    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.PENDING_VERIFICATION);
    verify(tenantRepository, never()).save(any());
  }

  @Test
  void reactivateChangesARestrictedTenantAndIsIdempotent() {
    Tenant tenant = activeTenant();
    tenant.restrict();
    when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));

    service.handle(new ReactivateTenantCommand(tenant.getTenantId()));
    service.handle(new ReactivateTenantCommand(tenant.getTenantId()));

    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    verify(tenantRepository).save(tenant);
  }

  @Test
  void restrictFailsWhenTheTenantDoesNotExist() {
    TenantId unknown = new TenantId(UUID.randomUUID());
    when(tenantRepository.findById(unknown.value())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.handle(new RestrictTenantCommand(unknown)))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
