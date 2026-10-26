package com.nexus.rinde.notification.application.internal.commandservices;

import com.nexus.rinde.iam.interfaces.acl.IamContextFacade;
import com.nexus.rinde.notification.domain.model.valueobjects.EmailMessage;
import com.nexus.rinde.notification.domain.model.valueobjects.RetryPolicy;
import com.nexus.rinde.notification.domain.services.EmailAdapter;
import com.nexus.rinde.notification.infrastructure.persistence.jpa.entities.EmailOutboxEntry;
import com.nexus.rinde.notification.infrastructure.persistence.jpa.repositories.EmailOutboxRepository;
import java.time.Clock;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Deja en la bandeja de correo simulado un aviso para el administrador, una sola vez por evento. */
@Component
public class AdministratorEmailOutbox {

  private static final Logger log = LoggerFactory.getLogger(AdministratorEmailOutbox.class);

  private final IamContextFacade iamContextFacade;
  private final EmailOutboxRepository emailOutboxRepository;
  private final EmailAdapter emailAdapter;
  private final RetryPolicy retryPolicy;
  private final Clock clock;

  public AdministratorEmailOutbox(
      IamContextFacade iamContextFacade,
      EmailOutboxRepository emailOutboxRepository,
      EmailAdapter emailAdapter,
      RetryPolicy retryPolicy,
      Clock clock) {
    this.iamContextFacade = iamContextFacade;
    this.emailOutboxRepository = emailOutboxRepository;
    this.emailAdapter = emailAdapter;
    this.retryPolicy = retryPolicy;
    this.clock = clock;
  }

  /** Ignora el evento si su correo ya quedó registrado; así un reenvío no duplica el aviso. */
  public void queueOnce(UUID eventId, UUID tenantId, String subject, String body) {
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
      String detail = ex.getMessage() == null ? "Falló la entrega del correo." : ex.getMessage();
      entry.markFailed(detail, retryPolicy, clock.instant());
      log.warn("El correo simulado quedó pendiente para el evento {}.", eventId, ex);
    }
    emailOutboxRepository.saveAndFlush(entry);
  }
}
