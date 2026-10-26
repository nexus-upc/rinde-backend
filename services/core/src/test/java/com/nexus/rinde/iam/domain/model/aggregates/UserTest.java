package com.nexus.rinde.iam.domain.model.aggregates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexus.rinde.iam.domain.model.events.PasswordResetRequested;
import com.nexus.rinde.iam.domain.model.events.UserInvited;
import com.nexus.rinde.iam.domain.model.valueobjects.Email;
import com.nexus.rinde.iam.domain.model.valueobjects.IssuedAccessToken;
import com.nexus.rinde.iam.domain.model.valueobjects.PasswordHash;
import com.nexus.rinde.iam.domain.model.valueobjects.Role;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.model.valueobjects.TokenPurpose;
import com.nexus.rinde.iam.domain.model.valueobjects.UserStatus;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

  private static final Instant NOW = Instant.parse("2026-10-06T10:00:00Z");
  private static final Duration INVITATION = Duration.ofHours(48);
  private static final Duration RESET = Duration.ofHours(1);

  private final TenantId tenantId = new TenantId(UUID.randomUUID());
  private final PasswordHash hash = new PasswordHash("$2a$10$hash");

  private User invitedUser() {
    User user = new User(tenantId, "Luis Quispe", new Email("luis@andes.pe"), Role.DRIVER);
    user.invite(INVITATION, NOW);
    return user;
  }

  private User activeUser() {
    return User.createAdministrator(tenantId, "Ana Rojas", new Email("ana@andes.pe"), hash);
  }

  private String invitationToken(User user) {
    UserInvited event = (UserInvited) user.pendingDomainEvents().get(0);
    return event.invitationToken();
  }

  @Test
  void administratorIsBornActiveWithItsPassword() {
    User admin = activeUser();

    assertThat(admin.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(admin.getRole()).isEqualTo(Role.ADMINISTRATOR);
    assertThat(admin.getPasswordHash()).isEqualTo(hash);
    assertThat(admin.belongsTo(tenantId)).isTrue();
    assertThat(admin.belongsTo(new TenantId(UUID.randomUUID()))).isFalse();
  }

  @Test
  void inviteLeavesTheUserInvitedWithoutPasswordAndPublishesTheEvent() {
    User user = invitedUser();

    assertThat(user.getStatus()).isEqualTo(UserStatus.INVITED);
    assertThat(user.getPasswordHash()).isNull();
    assertThat(user.getAccessTokens()).hasSize(1);
    assertThat(user.getAccessTokens().get(0).purpose()).isEqualTo(TokenPurpose.INVITATION);
    assertThat(user.getAccessTokens().get(0).expiresAt()).isEqualTo(NOW.plus(INVITATION));
    UserInvited event = (UserInvited) user.pendingDomainEvents().get(0);
    assertThat(event.email()).isEqualTo("luis@andes.pe");
    assertThat(event.role()).isEqualTo("DRIVER");
    assertThat(event.type()).isEqualTo("UserInvited");
    assertThat(event.toString()).doesNotContain(event.invitationToken());
  }

  @Test
  void cannotInviteTwice() {
    User user = invitedUser();

    assertThatThrownBy(() -> user.invite(INVITATION, NOW))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void definePasswordWithTheInvitationActivatesTheUserAndConsumesTheLink() {
    User user = invitedUser();
    String token = invitationToken(user);

    user.definePassword(token, hash, NOW.plusSeconds(60));

    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(user.getPasswordHash()).isEqualTo(hash);
    assertThat(user.getAccessTokens().get(0).used()).isTrue();
  }

  @Test
  void aLinkCannotBeUsedTwice() {
    User user = invitedUser();
    String token = invitationToken(user);
    user.definePassword(token, hash, NOW.plusSeconds(60));

    assertThatThrownBy(() -> user.definePassword(token, hash, NOW.plusSeconds(120)))
        .isInstanceOf(BusinessRuleException.class)
        .hasMessageContaining("enlace");
  }

  @Test
  void anExpiredLinkIsRejected() {
    User user = invitedUser();
    String token = invitationToken(user);

    assertThatThrownBy(() -> user.definePassword(token, hash, NOW.plus(INVITATION).plusSeconds(1)))
        .isInstanceOf(BusinessRuleException.class);
    assertThat(user.getStatus()).isEqualTo(UserStatus.INVITED);
  }

  @Test
  void anUnknownLinkIsRejected() {
    User user = invitedUser();

    assertThatThrownBy(() -> user.definePassword("desconocido", hash, NOW))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void aVerificationLinkCannotDefineAPassword() {
    User admin = activeUser();
    IssuedAccessToken verification =
        admin.issueAccessToken(TokenPurpose.EMAIL_VERIFICATION, INVITATION, NOW);

    assertThatThrownBy(() -> admin.definePassword(verification.rawValue(), hash, NOW))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void activateRequiresAnInvitedUser() {
    User admin = activeUser();

    assertThatThrownBy(() -> admin.activate(hash)).isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void passwordResetKeepsTheStatusAndChangesThePassword() {
    User user = activeUser();
    user.requestPasswordReset(RESET, NOW);
    PasswordResetRequested event = (PasswordResetRequested) user.pendingDomainEvents().get(0);
    PasswordHash newHash = new PasswordHash("$2a$10$new");

    user.definePassword(event.resetToken(), newHash, NOW.plusSeconds(600));

    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(user.getPasswordHash()).isEqualTo(newHash);
    assertThat(user.getAccessTokens().get(0).expiresAt()).isEqualTo(NOW.plus(RESET));
    assertThat(event.type()).isEqualTo("PasswordResetRequested");
    assertThat(event.toString()).doesNotContain(event.resetToken());
  }

  @Test
  void aPasswordResetLinkExpiresAfterOneHour() {
    User user = activeUser();
    user.requestPasswordReset(RESET, NOW);
    PasswordResetRequested event = (PasswordResetRequested) user.pendingDomainEvents().get(0);

    assertThatThrownBy(
            () -> user.definePassword(event.resetToken(), hash, NOW.plus(RESET).plusSeconds(1)))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void onlyAnActiveUserCanRequestAPasswordReset() {
    User invited = invitedUser();

    assertThatThrownBy(() -> invited.requestPasswordReset(RESET, NOW))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void changeRoleUpdatesTheRole() {
    User user = activeUser();

    user.changeRole(Role.OPERATIONS_MANAGER);

    assertThat(user.getRole()).isEqualTo(Role.OPERATIONS_MANAGER);
  }

  @Test
  void changeRoleRejectsNullAndDisabledUsers() {
    User user = activeUser();
    assertThatThrownBy(() -> user.changeRole(null)).isInstanceOf(BusinessRuleException.class);

    user.disable();

    assertThatThrownBy(() -> user.changeRole(Role.DRIVER))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void disableWorksFromInvitedAndActiveButNotTwice() {
    User invited = invitedUser();
    invited.disable();
    assertThat(invited.getStatus()).isEqualTo(UserStatus.DISABLED);
    assertThatThrownBy(invited::disable).isInstanceOf(BusinessRuleException.class);

    User active = activeUser();
    active.disable();
    assertThat(active.getStatus()).isEqualTo(UserStatus.DISABLED);
  }

  @Test
  void aDisabledUserCannotDefineAPassword() {
    User user = invitedUser();
    String token = invitationToken(user);
    user.disable();

    assertThatThrownBy(() -> user.definePassword(token, hash, NOW))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void validatesTheRequiredData() {
    Email email = new Email("a@b.com");

    assertThatThrownBy(() -> new User(tenantId, " ", email, Role.DRIVER))
        .isInstanceOf(BusinessRuleException.class);
    assertThatThrownBy(() -> new User(null, "Nombre", email, Role.DRIVER))
        .isInstanceOf(BusinessRuleException.class);
    assertThatThrownBy(() -> new User(tenantId, "Nombre", email, null))
        .isInstanceOf(BusinessRuleException.class);
  }
}
