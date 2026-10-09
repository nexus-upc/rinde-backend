package com.nexus.rinde.notification.application.internal.eventhandlers;

import com.nexus.rinde.notification.application.internal.commandservices.SubscriptionEmailNotificationService;
import com.nexus.rinde.subscription.interfaces.acl.PaymentReceiptRequested;
import com.nexus.rinde.subscription.interfaces.acl.SubscriptionExpiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Consume eventos de suscripción y crea correos locales después del commit del dominio. */
@Component
public class SubscriptionEmailEventHandler {

  private static final Logger log = LoggerFactory.getLogger(SubscriptionEmailEventHandler.class);

  private final SubscriptionEmailNotificationService notificationService;

  public SubscriptionEmailEventHandler(SubscriptionEmailNotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(SubscriptionExpiring event) {
    dispatch(() -> notificationService.handle(event), event.eventId().toString());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(PaymentReceiptRequested event) {
    dispatch(() -> notificationService.handle(event), event.eventId().toString());
  }

  private void dispatch(Runnable action, String eventId) {
    try {
      action.run();
    } catch (RuntimeException ex) {
      log.error("No se pudo registrar el correo del evento {}.", eventId, ex);
    }
  }
}
