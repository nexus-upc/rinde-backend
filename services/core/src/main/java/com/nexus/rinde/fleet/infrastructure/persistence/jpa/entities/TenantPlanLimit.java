package com.nexus.rinde.fleet.infrastructure.persistence.jpa.entities;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import com.nexus.rinde.subscription.interfaces.acl.PlanChanged;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Proyección local del plan que Fleet aplica al registrar unidades. */
@Entity
@Table(schema = "fleet", name = "tenant_plan_limits")
public class TenantPlanLimit {

  @Id
  @Column(name = "tenant_id", nullable = false, updatable = false)
  private UUID tenantId;

  @Column(name = "plan_id", nullable = false)
  private UUID planId;

  @Column(name = "unit_limit", nullable = false)
  private int unitLimit;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected TenantPlanLimit() {}

  private TenantPlanLimit(UUID tenantId, UUID planId, int unitLimit, Instant updatedAt) {
    if (tenantId == null || planId == null || unitLimit < 1 || updatedAt == null) {
      throw new BusinessRuleException("La empresa, el plan y su límite son obligatorios.");
    }
    this.tenantId = tenantId;
    this.planId = planId;
    this.unitLimit = unitLimit;
    this.updatedAt = updatedAt;
  }

  public static TenantPlanLimit from(PlanChanged event, Instant now) {
    return new TenantPlanLimit(event.tenantId(), event.planId(), event.unitLimit(), now);
  }

  public void apply(PlanChanged event, Instant now) {
    if (!tenantId.equals(event.tenantId()) || event.unitLimit() < 1) {
      throw new BusinessRuleException("El límite de plan recibido no es válido.");
    }
    planId = event.planId();
    unitLimit = event.unitLimit();
    updatedAt = now;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public UUID getPlanId() {
    return planId;
  }

  public int getUnitLimit() {
    return unitLimit;
  }
}
