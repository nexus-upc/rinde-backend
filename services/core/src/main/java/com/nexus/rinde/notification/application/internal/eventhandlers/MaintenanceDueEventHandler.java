package com.nexus.rinde.notification.application.internal.eventhandlers;

import com.nexus.rinde.fleet.interfaces.acl.MaintenanceDue;
import com.nexus.rinde.notification.application.internal.commandservices.MaintenanceDueNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Consume el aviso de mantenimiento de Fleet y crea el correo después del commit de Fleet. */
@Component
public class MaintenanceDueEventHandler {

  private static final Logger log = LoggerFactory.getLogger(MaintenanceDueEventHandler.class);

  private final MaintenanceDueNotificationService notificationService;

  public MaintenanceDueEventHandler(MaintenanceDueNotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(MaintenanceDue event) {
    try {
      notificationService.handle(event);
    } catch (RuntimeException ex) {
      log.error("No se pudo registrar el correo del evento {}.", event.eventId(), ex);
    }
  }
}
