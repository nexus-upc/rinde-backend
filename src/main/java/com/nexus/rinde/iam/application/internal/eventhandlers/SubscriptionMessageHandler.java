package com.nexus.rinde.iam.application.internal.eventhandlers;

import com.nexus.rinde.iam.application.events.SubscriptionStatusMessage;
import com.nexus.rinde.iam.domain.model.commands.ReactivateTenantCommand;
import com.nexus.rinde.iam.domain.model.commands.RestrictTenantCommand;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.services.TenantCommandService;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.entities.SubscriptionEventReceipt;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.SubscriptionEventReceiptRepository;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.TenantRepository;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Serializa por empresa y evita aplicar duplicados o estados anteriores a uno ya recibido. */
@Service
public class SubscriptionMessageHandler {

  private final TenantRepository tenants;
  private final SubscriptionEventReceiptRepository receipts;
  private final TenantCommandService commands;
  private final Clock clock;

  public SubscriptionMessageHandler(TenantRepository tenants, SubscriptionEventReceiptRepository receipts,
      TenantCommandService commands, Clock clock) {
    this.tenants = tenants;
    this.receipts = receipts;
    this.commands = commands;
    this.clock = clock;
  }

  @Transactional
  public void handle(SubscriptionStatusMessage event, String routingKey) {
    if (!"subscription.activated".equals(routingKey) && !"subscription.suspended".equals(routingKey)) {
      throw new IllegalArgumentException("El tipo de evento de suscripción no es válido.");
    }
    tenants.findByIdForSubscriptionUpdate(event.tenantId())
        .orElseThrow(() -> new ResourceNotFoundException("La empresa del evento no existe."));
    if (receipts.existsById(event.eventId())) {
      return;
    }
    boolean stale = receipts.findFirstByTenantIdOrderByOccurredAtDesc(event.tenantId())
        .map(last -> event.occurredAt().isBefore(last.getOccurredAt())).orElse(false);
    if (!stale) {
      TenantId tenantId = new TenantId(event.tenantId());
      if ("subscription.activated".equals(routingKey)) {
        commands.handle(new ReactivateTenantCommand(tenantId));
      } else {
        commands.handle(new RestrictTenantCommand(tenantId));
      }
    }
    receipts.saveAndFlush(new SubscriptionEventReceipt(event.eventId(), event.tenantId(), routingKey,
        event.occurredAt(), clock.instant()));
  }
}
