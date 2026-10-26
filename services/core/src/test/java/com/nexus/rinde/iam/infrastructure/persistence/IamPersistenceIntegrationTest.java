package com.nexus.rinde.iam.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.domain.model.events.TenantRegistered;
import com.nexus.rinde.iam.domain.model.events.UserInvited;
import com.nexus.rinde.iam.domain.model.valueobjects.AccessToken;
import com.nexus.rinde.iam.domain.model.valueobjects.Email;
import com.nexus.rinde.iam.domain.model.valueobjects.PasswordHash;
import com.nexus.rinde.iam.domain.model.valueobjects.Role;
import com.nexus.rinde.iam.domain.model.valueobjects.Ruc;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantStatus;
import com.nexus.rinde.iam.domain.model.valueobjects.TokenPurpose;
import com.nexus.rinde.iam.domain.model.valueobjects.UserStatus;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.TenantRepository;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.nexus.rinde.support.AbstractIntegrationTest;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

class IamPersistenceIntegrationTest extends AbstractIntegrationTest {

  private static final Instant NOW = Instant.parse("2026-10-06T10:00:00Z");

  @Autowired private TenantRepository tenants;
  @Autowired private UserRepository users;
  @Autowired private TransactionTemplate transaction;

  private Tenant saveRegisteredTenant(String ruc) {
    Tenant tenant = new Tenant("Transportes Andes", new Ruc(ruc));
    tenant.register(
        new Email("admin@andes.pe"),
        AccessToken.issue(
            tenant.getTenantId(), TokenPurpose.EMAIL_VERIFICATION, Duration.ofHours(48), NOW),
        NOW);
    return tenants.save(tenant);
  }

  @Test
  void savesATenantAndPublishesItsEventsOnSave() {
    Tenant saved = saveRegisteredTenant("20123456789");

    Tenant reloaded = tenants.findById(saved.getId()).orElseThrow();
    assertThat(reloaded.getStatus()).isEqualTo(TenantStatus.PENDING_VERIFICATION);
    assertThat(reloaded.getRuc()).isEqualTo(new Ruc("20123456789"));
    assertThat(reloaded.getCreatedAt()).isNotNull();
    assertThat(tenants.existsByRuc(new Ruc("20123456789"))).isTrue();
    assertThat(tenants.existsByRuc(new Ruc("20999999999"))).isFalse();
    assertThat(capturedEvents.last(TenantRegistered.class)).isPresent();
    assertThat(saved.pendingDomainEvents()).isEmpty();
  }

  @Test
  void aTenantUpdateKeepsTheSameRowAndRefreshesUpdatedAt() {
    Tenant saved = saveRegisteredTenant("20123456789");
    saved.verify();
    tenants.save(saved);

    Tenant reloaded = tenants.findById(saved.getId()).orElseThrow();

    assertThat(reloaded.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    assertThat(tenants.count()).isEqualTo(1);
  }

  @Test
  void rejectsADuplicatedRucAtDatabaseLevel() {
    saveRegisteredTenant("20123456789");

    assertThatThrownBy(() -> saveRegisteredTenant("20123456789"))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void savesAUserWithItsAccessTokensAndFindsItByTokenHash() {
    Tenant tenant = saveRegisteredTenant("20123456789");
    User user =
        new User(tenant.getTenantId(), "Luis Quispe", new Email("Luis@Andes.pe"), Role.DRIVER);
    user.invite(Duration.ofHours(48), NOW);
    users.save(user);
    String rawToken = capturedEvents.last(UserInvited.class).orElseThrow().invitationToken();

    User found = users.findByAccessTokenHash(AccessToken.hash(rawToken)).orElseThrow();

    assertThat(found.getId()).isEqualTo(user.getId());
    assertThat(users.findByEmail(new Email("luis@andes.pe"))).isPresent();
    assertThat(users.existsByEmail(new Email("LUIS@andes.pe"))).isTrue();
    assertThat(users.findByTenantId(tenant.getTenantId())).hasSize(1);
    transaction.executeWithoutResult(
        status -> {
          User loaded = users.findById(user.getId()).orElseThrow();
          assertThat(loaded.getStatus()).isEqualTo(UserStatus.INVITED);
          assertThat(loaded.getPasswordHash()).isNull();
          assertThat(loaded.getAccessTokens()).hasSize(1);
          assertThat(loaded.getAccessTokens().get(0).purpose()).isEqualTo(TokenPurpose.INVITATION);
        });
  }

  @Test
  void persistsTheUseOfAnAccessTokenAndTheNewPassword() {
    Tenant tenant = saveRegisteredTenant("20123456789");
    User user =
        new User(tenant.getTenantId(), "Luis Quispe", new Email("luis@andes.pe"), Role.DRIVER);
    user.invite(Duration.ofHours(48), NOW);
    users.save(user);
    String rawToken = capturedEvents.last(UserInvited.class).orElseThrow().invitationToken();

    transaction.executeWithoutResult(
        status -> {
          User loaded = users.findByAccessTokenHash(AccessToken.hash(rawToken)).orElseThrow();
          loaded.definePassword(rawToken, new PasswordHash("$2a$10$hash"), NOW.plusSeconds(30));
          users.save(loaded);
        });

    transaction.executeWithoutResult(
        status -> {
          User loaded = users.findById(user.getId()).orElseThrow();
          assertThat(loaded.getStatus()).isEqualTo(UserStatus.ACTIVE);
          assertThat(loaded.getPasswordHash()).isEqualTo(new PasswordHash("$2a$10$hash"));
          assertThat(loaded.getAccessTokens()).hasSize(1);
          assertThat(loaded.getAccessTokens().get(0).used()).isTrue();
        });
  }

  @Test
  void rejectsADuplicatedEmailAtDatabaseLevel() {
    Tenant tenant = saveRegisteredTenant("20123456789");
    users.save(
        User.createAdministrator(
            tenant.getTenantId(), "Ana", new Email("ana@andes.pe"), new PasswordHash("h")));

    assertThatThrownBy(
            () ->
                users.save(
                    User.createAdministrator(
                        tenant.getTenantId(),
                        "Otra Ana",
                        new Email("ana@andes.pe"),
                        new PasswordHash("h"))))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
