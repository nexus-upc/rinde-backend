package com.nexus.rinde.fleet.application.internal.eventhandlers;

import com.nexus.rinde.fleet.infrastructure.persistence.jpa.entities.TenantPlanLimit;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.TenantPlanLimitRepository;
import com.nexus.rinde.subscription.interfaces.acl.PlanChanged;
import java.time.Clock;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Actualiza en Fleet el límite local anunciado tras confirmar un pago de suscripción. */
@Component
public class PlanChangedEventHandler {

  private final TenantPlanLimitRepository planLimitRepository;
  private final Clock clock;

  public PlanChangedEventHandler(TenantPlanLimitRepository planLimitRepository, Clock clock) {
    this.planLimitRepository = planLimitRepository;
    this.clock = clock;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(PlanChanged event) {
    TenantPlanLimit limit =
        planLimitRepository
            .findById(event.tenantId())
            .map(
                existing -> {
                  existing.apply(event, clock.instant());
                  return existing;
                })
            .orElseGet(() -> TenantPlanLimit.from(event, clock.instant()));
    planLimitRepository.saveAndFlush(limit);
  }
}
