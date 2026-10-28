package com.nexus.rinde.fleet.interfaces.acl;

import com.nexus.rinde.shared.domain.model.events.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;

/** Evento de cambio de plan emitido por Subscriptions y consumido por Fleet vía RabbitMQ. */
public record PlanChanged(
    UUID eventId, Instant occurredAt, UUID tenantId, UUID planId, int unitLimit)
    implements IntegrationEvent {

  @Override
  public String type() {
    return "PlanChanged";
  }
}
