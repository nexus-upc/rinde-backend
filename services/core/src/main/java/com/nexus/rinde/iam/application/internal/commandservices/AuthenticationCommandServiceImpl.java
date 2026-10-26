package com.nexus.rinde.iam.application.internal.commandservices;

import com.nexus.rinde.iam.application.internal.TokenValidityProperties;
import com.nexus.rinde.iam.application.internal.outboundservices.AuthenticationTokenService;
import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.domain.model.commands.DefinePasswordCommand;
import com.nexus.rinde.iam.domain.model.commands.RequestPasswordResetCommand;
import com.nexus.rinde.iam.domain.model.commands.SignInCommand;
import com.nexus.rinde.iam.domain.model.valueobjects.AccessToken;
import com.nexus.rinde.iam.domain.model.valueobjects.AuthenticationResult;
import com.nexus.rinde.iam.domain.model.valueobjects.Email;
import com.nexus.rinde.iam.domain.model.valueobjects.PasswordHash;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantStatus;
import com.nexus.rinde.iam.domain.model.valueobjects.UserStatus;
import com.nexus.rinde.iam.domain.services.AuthenticationCommandService;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.TenantRepository;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.nexus.rinde.shared.domain.exceptions.AuthenticationFailedException;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import com.nexus.rinde.shared.domain.exceptions.ForbiddenOperationException;
import java.time.Clock;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicio de sesión, solicitud de recuperación de contraseña y definición de contraseña con enlace.
 */
@Service
public class AuthenticationCommandServiceImpl implements AuthenticationCommandService {

  private static final String BAD_CREDENTIALS = "El correo o la contraseña son incorrectos.";

  private final UserRepository userRepository;
  private final TenantRepository tenantRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationTokenService tokenService;
  private final TokenValidityProperties tokenValidity;
  private final Clock clock;
  private final String dummyHash;

  public AuthenticationCommandServiceImpl(
      UserRepository userRepository,
      TenantRepository tenantRepository,
      PasswordEncoder passwordEncoder,
      AuthenticationTokenService tokenService,
      TokenValidityProperties tokenValidity,
      Clock clock) {
    this.userRepository = userRepository;
    this.tenantRepository = tenantRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
    this.tokenValidity = tokenValidity;
    this.clock = clock;
    this.dummyHash = passwordEncoder.encode("rinde-timing-equalizer");
  }

  /**
   * Valida las credenciales y emite el token. Una empresa pendiente de verificación no puede
   * ingresar; una restringida sí, y su estado viaja en el token para que otros contextos limiten.
   */
  @Override
  @Transactional(readOnly = true)
  public AuthenticationResult handle(SignInCommand command) {
    User user = findByEmail(command.email()).orElse(null);
    String storedHash =
        user != null && user.getPasswordHash() != null ? user.getPasswordHash().value() : dummyHash;
    // Se compara siempre contra un hash para que el tiempo no revele si el correo existe.
    boolean matches = passwordEncoder.matches(command.password(), storedHash);
    if (user == null || user.getPasswordHash() == null || !matches) {
      throw new AuthenticationFailedException(BAD_CREDENTIALS);
    }
    if (user.getStatus() == UserStatus.DISABLED) {
      throw new ForbiddenOperationException("El usuario está deshabilitado.");
    }
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new AuthenticationFailedException(BAD_CREDENTIALS);
    }
    Tenant tenant =
        tenantRepository
            .findById(user.getTenantId().value())
            .orElseThrow(() -> new IllegalStateException("El usuario no tiene empresa."));
    if (tenant.getStatus() == TenantStatus.PENDING_VERIFICATION) {
      throw new ForbiddenOperationException(
          "La empresa aún no confirmó su correo. Use el enlace de verificación que se le envió.");
    }
    return tokenService.issueFor(user, tenant.getStatus());
  }

  /** No revela si el correo existe: si no hay un usuario activo con ese correo, no hace nada. */
  @Override
  @Transactional
  public void handle(RequestPasswordResetCommand command) {
    findByEmail(command.email())
        .filter(user -> user.getStatus() == UserStatus.ACTIVE)
        .ifPresent(
            user -> {
              user.requestPasswordReset(tokenValidity.passwordResetValidity(), clock.instant());
              userRepository.save(user);
            });
  }

  /**
   * Sirve tanto para aceptar una invitación (activa al usuario) como para recuperar la contraseña.
   */
  @Override
  @Transactional
  public void handle(DefinePasswordCommand command) {
    User user =
        userRepository
            .findByAccessTokenHash(AccessToken.hash(command.token()))
            .orElseThrow(() -> new BusinessRuleException(User.INVALID_LINK_MESSAGE));
    user.definePassword(
        command.token(),
        new PasswordHash(passwordEncoder.encode(command.password())),
        clock.instant());
    userRepository.save(user);
  }

  /** Un correo con formato inválido se trata como inexistente. */
  private Optional<User> findByEmail(String rawEmail) {
    try {
      return userRepository.findByEmail(new Email(rawEmail));
    } catch (BusinessRuleException ex) {
      return Optional.empty();
    }
  }
}
