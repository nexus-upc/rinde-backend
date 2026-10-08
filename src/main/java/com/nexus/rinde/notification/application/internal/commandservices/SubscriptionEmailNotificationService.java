package com.nexus.rinde.notification.application.internal.commandservices;

import com.nexus.rinde.iam.interfaces.acl.IamContextFacade;
import com.nexus.rinde.notification.domain.model.valueobjects.EmailMessage;
import com.nexus.rinde.notification.domain.services.EmailAdapter;
import com.nexus.rinde.notification.infrastructure.persistence.jpa.entities.EmailOutboxEntry;
import com.nexus.rinde.notification.infrastructure.persistence.jpa.repositories.EmailOutboxRepository;
import com.nexus.rinde.subscription.interfaces.acl.PaymentReceiptRequested;
import com.nexus.rinde.subscription.interfaces.acl.SubscriptionExpiring;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Envía y registra comprobantes y avisos mediante el correo simulado del proyecto. */
@Service
public class SubscriptionEmailNotificationService {

  private static final Logger log =
      LoggerFactory.getLogger(SubscriptionEmailNotificationService.class);

  private final IamContextFacade iamContextFacade;
  private final EmailOutboxRepository emailOutboxRepository;
  private final EmailAdapter emailAdapter;
  private final Clock clock;

  public SubscriptionEmailNotificationService(
      IamContextFacade iamContextFacade,
      EmailOutboxRepository emailOutboxRepository,
      EmailAdapter emailAdapter,
      Clock clock) {
    this.iamContextFacade = iamContextFacade;
    this.emailOutboxRepository = emailOutboxRepository;
    this.emailAdapter = emailAdapter;
    this.clock = clock;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handle(SubscriptionExpiring event) {
    String expiry = DateTimeFormatter.ISO_INSTANT.format(event.expiresAt());
    sendOnce(
        event.eventId(),
        event.tenantId(),
        "Tu suscripción vence pronto",
        "Tu suscripción vence el " + expiry + ". Renueva el plan para mantener habilitada la empresa.");
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handle(PaymentReceiptRequested event) {
    String body =
        "Pago simulado confirmado. Referencia: "
            + event.providerReference()
            + ". Importe: "
            + event.amount().toPlainString()
            + ". Periodo: "
            + DateTimeFormatter.ISO_INSTANT.format(event.startsAt())
            + " a "
            + DateTimeFormatter.ISO_INSTANT.format(event.expiresAt())
            + ". RINDE no recibió ni almacenó datos de tarjeta.";
    sendOnce(
        event.eventId(), event.tenantId(), "Comprobante de suscripción RINDE", body);
  }

  private void sendOnce(
      java.util.UUID eventId, java.util.UUID tenantId, String subject, String body) {
    if (emailOutboxRepository.existsById(eventId)) {
      return;
    }
    var recipient = iamContextFacade.findAdministratorEmail(tenantId);
    if (recipient.isEmpty()) {
      log.warn("No se pudo resolver el correo del administrador para la empresa {}.", tenantId);
      return;
    }
    EmailMessage message = new EmailMessage(eventId, tenantId, recipient.get(), subject, body);
    EmailOutboxEntry entry =
        emailOutboxRepository.saveAndFlush(EmailOutboxEntry.pending(message, clock.instant()));
    try {
      emailAdapter.send(message);
      entry.markSimulatedSent(clock.instant());
    } catch (RuntimeException ex) {
      entry.markPending(ex.getMessage() == null ? "Falló la entrega del correo." : ex.getMessage());
      log.warn("El correo simulado quedó pendiente para el evento {}.", eventId, ex);
    }
    emailOutboxRepository.saveAndFlush(entry);
  }
}
