package com.nexus.rinde.iam.domain.model.aggregates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexus.rinde.iam.domain.model.events.TenantRegistered;
import com.nexus.rinde.iam.domain.model.valueobjects.AccessToken;
import com.nexus.rinde.iam.domain.model.valueobjects.Email;
import com.nexus.rinde.iam.domain.model.valueobjects.IssuedAccessToken;
import com.nexus.rinde.iam.domain.model.valueobjects.Ruc;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantStatus;
import com.nexus.rinde.iam.domain.model.valueobjects.TokenPurpose;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TenantTest {

  private static final Instant NOW = Instant.parse("2026-10-06T10:00:00Z");

  private Tenant newTenant() {
    return new Tenant("Transportes Andes", new Ruc("20123456789"));
  }

  private IssuedAccessToken verificationToken(Tenant tenant) {
    return AccessToken.issue(
        tenant.getTenantId(), TokenPurpose.EMAIL_VERIFICATION, Duration.ofHours(48), NOW);
  }

  private Tenant registeredTenant() {
    Tenant tenant = newTenant();
    tenant.register(new Email("admin@andes.pe"), verificationToken(tenant), NOW);
    return tenant;
  }

  @Test
  void aNewTenantHasNoStatusUntilItIsRegistered() {
    assertThat(newTenant().getStatus()).isNull();
  }

  @Test
  void registerLeavesTheTenantPendingVerificationAndPublishesTheEvent() {
    Tenant tenant = registeredTenant();

    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.PENDING_VERIFICATION);
    assertThat(tenant.pendingDomainEvents()).hasSize(1);
    TenantRegistered event = (TenantRegistered) tenant.pendingDomainEvents().get(0);
    assertThat(event.tenantId()).isEqualTo(tenant.getId());
    assertThat(event.administratorEmail()).isEqualTo("admin@andes.pe");
    assertThat(event.verificationToken()).isNotBlank();
    assertThat(event.type()).isEqualTo("TenantRegistered");
    assertThat(event.toString()).doesNotContain(event.verificationToken());
  }

  @Test
  void cannotRegisterTwice() {
    Tenant tenant = registeredTenant();
    IssuedAccessToken token = verificationToken(tenant);

    assertThatThrownBy(() -> tenant.register(new Email("otro@andes.pe"), token, NOW))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void verifyActivatesAPendingTenant() {
    Tenant tenant = registeredTenant();

    tenant.verify();

    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
  }

  @Test
  void cannotVerifyTwice() {
    Tenant tenant = registeredTenant();
    tenant.verify();

    assertThatThrownBy(tenant::verify).isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void restrictAndReactivateAlternateBetweenActiveAndRestricted() {
    Tenant tenant = registeredTenant();
    tenant.verify();

    tenant.restrict();
    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.RESTRICTED);

    tenant.reactivate();
    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
  }

  @Test
  void cannotRestrictATenantThatIsNotActive() {
    Tenant pending = registeredTenant();

    assertThatThrownBy(pending::restrict).isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void cannotReactivateATenantThatIsNotRestricted() {
    Tenant tenant = registeredTenant();
    tenant.verify();

    assertThatThrownBy(tenant::reactivate).isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void requiresTradeNameAndRuc() {
    Ruc ruc = new Ruc("20123456789");

    assertThatThrownBy(() -> new Tenant(" ", ruc)).isInstanceOf(BusinessRuleException.class);
    assertThatThrownBy(() -> new Tenant("Empresa", null)).isInstanceOf(BusinessRuleException.class);
    assertThatThrownBy(() -> new Tenant("x".repeat(121), ruc))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void exposesItsIdAsTenantId() {
    Tenant tenant = newTenant();

    assertThat(tenant.getTenantId()).isEqualTo(new TenantId(tenant.getId()));
  }
}
