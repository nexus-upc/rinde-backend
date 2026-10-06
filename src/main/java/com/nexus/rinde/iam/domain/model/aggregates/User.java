package com.nexus.rinde.iam.domain.model.aggregates;

import com.nexus.rinde.iam.domain.model.events.PasswordResetRequested;
import com.nexus.rinde.iam.domain.model.events.UserInvited;
import com.nexus.rinde.iam.domain.model.valueobjects.AccessToken;
import com.nexus.rinde.iam.domain.model.valueobjects.Email;
import com.nexus.rinde.iam.domain.model.valueobjects.IssuedAccessToken;
import com.nexus.rinde.iam.domain.model.valueobjects.PasswordHash;
import com.nexus.rinde.iam.domain.model.valueobjects.Role;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.model.valueobjects.TokenPurpose;
import com.nexus.rinde.iam.domain.model.valueobjects.UserId;
import com.nexus.rinde.iam.domain.model.valueobjects.UserStatus;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import com.nexus.rinde.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Persona que usa RINDE dentro de una empresa. Referencia a la empresa solo por su tenantId y
 * guarda los enlaces de acceso (invitación, recuperación y verificación) con su hash.
 */
@Entity
@Table(schema = "iam", name = "users")
public class User extends AuditableAbstractAggregateRoot {

  public static final String INVALID_LINK_MESSAGE =
      "El enlace no es válido, venció o ya fue utilizado.";

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private TenantId tenantId;

  @Column(name = "full_name", nullable = false, length = 150)
  private String fullName;

  @Column(name = "email", nullable = false, length = 150)
  private Email email;

  @Column(name = "password_hash", length = 255)
  private PasswordHash passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 30)
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private UserStatus status;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      schema = "iam",
      name = "access_tokens",
      joinColumns = @JoinColumn(name = "user_id"))
  private List<AccessToken> accessTokens = new ArrayList<>();

  protected User() {}

  public User(TenantId tenantId, String fullName, Email email, Role role) {
    if (tenantId == null || email == null || role == null) {
      throw new BusinessRuleException(
          "La empresa, el correo y el rol del usuario son obligatorios.");
    }
    if (fullName == null || fullName.isBlank() || fullName.length() > 150) {
      throw new BusinessRuleException(
          "El nombre completo es obligatorio y admite hasta 150 caracteres.");
    }
    this.tenantId = tenantId;
    this.fullName = fullName.trim();
    this.email = email;
    this.role = role;
  }

  /** Crea al administrador que registra la empresa: nace activo con la contraseña que ingresó. */
  public static User createAdministrator(
      TenantId tenantId, String fullName, Email email, PasswordHash passwordHash) {
    User user = new User(tenantId, fullName, email, Role.ADMINISTRATOR);
    user.status = UserStatus.ACTIVE;
    user.passwordHash = passwordHash;
    return user;
  }

  /** Deja al usuario invitado y emite su enlace para definir la contraseña. */
  public void invite(Duration validity, Instant now) {
    if (status != null) {
      throw new BusinessRuleException("El usuario ya fue invitado.");
    }
    status = UserStatus.INVITED;
    IssuedAccessToken issued = issueAccessToken(TokenPurpose.INVITATION, validity, now);
    registerEvent(
        new UserInvited(
            UUID.randomUUID(),
            now,
            tenantId.value(),
            getId(),
            fullName,
            email.address(),
            role.name(),
            issued.rawValue(),
            issued.token().expiresAt()));
  }

  /** Activa al usuario invitado al definir su contraseña. */
  public void activate(PasswordHash password) {
    if (status != UserStatus.INVITED) {
      throw new BusinessRuleException("Solo un usuario invitado puede activarse.");
    }
    this.passwordHash = password;
    this.status = UserStatus.ACTIVE;
  }

  /** Define la contraseña con un enlace de invitación o de recuperación, y lo marca como usado. */
  public void definePassword(String rawToken, PasswordHash password, Instant now) {
    if (status == UserStatus.DISABLED) {
      throw new BusinessRuleException("El usuario está deshabilitado.");
    }
    TokenPurpose purpose =
        consumeAccessToken(rawToken, now, TokenPurpose.INVITATION, TokenPurpose.PASSWORD_RESET);
    if (purpose == TokenPurpose.INVITATION) {
      activate(password);
    } else {
      this.passwordHash = password;
    }
  }

  public void changeRole(Role newRole) {
    if (newRole == null) {
      throw new BusinessRuleException("El rol es obligatorio.");
    }
    if (status == UserStatus.DISABLED) {
      throw new BusinessRuleException("No se puede cambiar el rol de un usuario deshabilitado.");
    }
    this.role = newRole;
  }

  public void disable() {
    if (status == UserStatus.DISABLED || status == null) {
      throw new BusinessRuleException("El usuario ya está deshabilitado.");
    }
    this.status = UserStatus.DISABLED;
  }

  /** Emite un enlace de recuperación; la contraseña actual sigue válida hasta que se use. */
  public void requestPasswordReset(Duration validity, Instant now) {
    if (status != UserStatus.ACTIVE) {
      throw new BusinessRuleException("Solo un usuario activo puede recuperar su contraseña.");
    }
    IssuedAccessToken issued = issueAccessToken(TokenPurpose.PASSWORD_RESET, validity, now);
    registerEvent(
        new PasswordResetRequested(
            UUID.randomUUID(),
            now,
            tenantId.value(),
            getId(),
            email.address(),
            issued.rawValue(),
            issued.token().expiresAt()));
  }

  /** Emite y guarda un enlace de acceso; devuelve el valor real, que no se vuelve a poder leer. */
  public IssuedAccessToken issueAccessToken(TokenPurpose purpose, Duration validity, Instant now) {
    IssuedAccessToken issued = AccessToken.issue(tenantId, purpose, validity, now);
    accessTokens.add(issued.token());
    return issued;
  }

  /**
   * Valida un enlace (existe, es de un propósito permitido, está vigente y sin usar) y lo marca
   * como usado. Devuelve su propósito.
   */
  public TokenPurpose consumeAccessToken(String rawToken, Instant now, TokenPurpose... allowed) {
    String hash = AccessToken.hash(rawToken);
    List<TokenPurpose> allowedPurposes = List.of(allowed);
    for (int i = 0; i < accessTokens.size(); i++) {
      AccessToken token = accessTokens.get(i);
      if (token.value().equals(hash)) {
        if (!token.isUsable(now) || !allowedPurposes.contains(token.purpose())) {
          throw new BusinessRuleException(INVALID_LINK_MESSAGE);
        }
        accessTokens.set(i, token.markUsed(now));
        return token.purpose();
      }
    }
    throw new BusinessRuleException(INVALID_LINK_MESSAGE);
  }

  public boolean belongsTo(TenantId otherTenantId) {
    return tenantId.equals(otherTenantId);
  }

  public UserId getUserId() {
    return new UserId(getId());
  }

  public TenantId getTenantId() {
    return tenantId;
  }

  public String getFullName() {
    return fullName;
  }

  public Email getEmail() {
    return email;
  }

  public PasswordHash getPasswordHash() {
    return passwordHash;
  }

  public Role getRole() {
    return role;
  }

  public UserStatus getStatus() {
    return status;
  }

  public List<AccessToken> getAccessTokens() {
    return Collections.unmodifiableList(accessTokens);
  }
}
