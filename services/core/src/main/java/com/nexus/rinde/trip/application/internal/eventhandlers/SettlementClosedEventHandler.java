package com.nexus.rinde.trip.application.internal.eventhandlers;

import com.nexus.rinde.settlement.interfaces.acl.SettlementClosed;
import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.domain.model.valueobjects.TripStatus;
import com.nexus.rinde.trip.infrastructure.persistence.jpa.repositories.TripRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Marca como liquidado el viaje informado por SettlementClosed; los duplicados son inocuos. */
@Component
public class SettlementClosedEventHandler {

  private final TripRepository tripRepository;

  public SettlementClosedEventHandler(TripRepository tripRepository) {
    this.tripRepository = tripRepository;
  }

  @EventListener
  @Transactional
  public void on(SettlementClosed event) {
    tripRepository
        .findByIdAndTenantId(event.tripId(), event.tenantId())
        .filter(trip -> trip.getStatus() != TripStatus.SETTLED)
        .ifPresent(trip -> markSettled(trip, event));
  }

  private void markSettled(Trip trip, SettlementClosed event) {
    trip.markSettled(event.closedBy(), event.occurredAt());
    tripRepository.saveAndFlush(trip);
  }
}
