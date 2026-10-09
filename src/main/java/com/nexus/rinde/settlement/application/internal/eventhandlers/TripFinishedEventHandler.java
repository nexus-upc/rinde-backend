package com.nexus.rinde.settlement.application.internal.eventhandlers;

import com.nexus.rinde.settlement.domain.model.aggregates.Settlement;
import com.nexus.rinde.settlement.infrastructure.persistence.jpa.repositories.SettlementRepository;
import com.nexus.rinde.trip.domain.model.events.TripFinished;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Abre una liquidación cuando el viaje finaliza; ignora duplicados (idempotente). */
@Component
public class TripFinishedEventHandler {

  private final SettlementRepository settlementRepository;

  public TripFinishedEventHandler(SettlementRepository settlementRepository) {
    this.settlementRepository = settlementRepository;
  }

  @EventListener
  @Transactional
  public void on(TripFinished event) {
    if (settlementRepository.existsByTripId(event.tripId())) {
      return;
    }
    Settlement settlement =
        Settlement.create(event.tenantId(), event.tripId(), event.driverId());
    settlementRepository.save(settlement);
  }
}
