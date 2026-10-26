package com.nexus.rinde.trip.application.internal.eventhandlers;

import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.domain.model.valueobjects.TripStatus;
import com.nexus.rinde.trip.infrastructure.persistence.jpa.entities.ProcessedEvent;
import com.nexus.rinde.trip.infrastructure.persistence.jpa.repositories.ProcessedEventRepository;
import com.nexus.rinde.trip.infrastructure.persistence.jpa.repositories.TripRepository;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Marca como liquidado el viaje informado por settlement.closed. Cada eventId se procesa una sola
 * vez: el registro del evento se guarda en la misma transacción que el cambio de estado.
 */
@Service
public class SettlementClosedEventHandler {

  private static final Logger log = LoggerFactory.getLogger(SettlementClosedEventHandler.class);

  private final TripRepository tripRepository;
  private final ProcessedEventRepository processedEventRepository;
  private final Clock clock;

  public SettlementClosedEventHandler(
      TripRepository tripRepository,
      ProcessedEventRepository processedEventRepository,
      Clock clock) {
    this.tripRepository = tripRepository;
    this.processedEventRepository = processedEventRepository;
    this.clock = clock;
  }

  @Transactional
  public void handle(SettlementClosed event) {
    if (processedEventRepository.existsById(event.eventId())) {
      log.info("Evento {} ya procesado; se ignora el duplicado.", event.eventId());
      return;
    }
    tripRepository
        .findByIdAndTenantId(event.tripId(), event.tenantId())
        .ifPresentOrElse(
            trip -> settle(trip, event),
            () -> log.warn("SettlementClosed {} referencia un viaje inexistente.", event.eventId()));
    processedEventRepository.save(
        new ProcessedEvent(event.eventId(), event.type(), clock.instant()));
  }

  private void settle(Trip trip, SettlementClosed event) {
    if (trip.getStatus() == TripStatus.SETTLED) {
      return;
    }
    trip.markSettled(event.closedBy(), event.occurredAt());
    tripRepository.saveAndFlush(trip);
  }
}
