package com.nexus.rinde.notification.application.internal.eventhandlers;

import com.nexus.rinde.notification.application.internal.commandservices.TripAssignmentNotificationService;
import com.nexus.rinde.trip.domain.model.events.TripAssigned;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Consume TripAssigned después del commit para que la notificación no revierta la asignación. */
@Component
public class TripAssignedEventHandler {

  private static final Logger log = LoggerFactory.getLogger(TripAssignedEventHandler.class);

  private final TripAssignmentNotificationService notificationService;

  public TripAssignedEventHandler(TripAssignmentNotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(TripAssigned event) {
    try {
      notificationService.handle(event);
    } catch (RuntimeException ex) {
      log.error(
          "No se pudo preparar el aviso para TripAssigned {}. La asignación ya quedó confirmada.",
          event.eventId(),
          ex);
    }
  }
}
