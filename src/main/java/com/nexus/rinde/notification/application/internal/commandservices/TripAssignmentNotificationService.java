package com.nexus.rinde.notification.application.internal.commandservices;

import com.nexus.rinde.notification.domain.model.valueobjects.RetryPolicy;
import com.nexus.rinde.notification.domain.model.valueobjects.TripAssignmentNotice;
import com.nexus.rinde.notification.domain.services.PushAdapter;
import com.nexus.rinde.notification.infrastructure.persistence.jpa.entities.RetryStoreEntry;
import com.nexus.rinde.notification.infrastructure.persistence.jpa.repositories.RetryStoreRepository;
import com.nexus.rinde.notification.infrastructure.services.PushAdapterUnavailableException;
import com.nexus.rinde.trip.domain.model.events.TripAssigned;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Prepara el aviso de asignación y lo conserva si la entrega push no está disponible. */
@Service
public class TripAssignmentNotificationService {

  private static final Logger log = LoggerFactory.getLogger(TripAssignmentNotificationService.class);

  private final RetryStoreRepository retryStoreRepository;
  private final PushAdapter pushAdapter;
  private final RetryPolicy retryPolicy;
  private final Clock clock;

  public TripAssignmentNotificationService(
      RetryStoreRepository retryStoreRepository,
      PushAdapter pushAdapter,
      RetryPolicy retryPolicy,
      Clock clock) {
    this.retryStoreRepository = retryStoreRepository;
    this.pushAdapter = pushAdapter;
    this.retryPolicy = retryPolicy;
    this.clock = clock;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handle(TripAssigned event) {
    if (retryStoreRepository.existsById(event.eventId())) {
      return;
    }

    TripAssignmentNotice notice =
        TripAssignmentNotice.from(
            event.eventId(),
            event.tenantId(),
            event.tripId(),
            event.driverId(),
            event.code(),
            event.destination(),
            event.departureDate(),
            event.occurredAt());
    RetryStoreEntry entry =
        retryStoreRepository.saveAndFlush(RetryStoreEntry.prepared(notice, clock.instant()));

    try {
      pushAdapter.send(notice);
      entry.markDelivered(clock.instant());
    } catch (PushAdapterUnavailableException ex) {
      entry.markFailed(ex.getMessage(), false, retryPolicy, clock.instant());
      log.info("Aviso push pendiente para el conductor {}: {}", event.driverId(), ex.getMessage());
    } catch (RuntimeException ex) {
      String detail = ex.getMessage() == null ? "Falló la entrega push." : ex.getMessage();
      entry.markFailed(detail, false, retryPolicy, clock.instant());
      log.warn("Falló la entrega push del evento {} y quedó pendiente.", event.eventId(), ex);
    }
    retryStoreRepository.saveAndFlush(entry);
  }
}
