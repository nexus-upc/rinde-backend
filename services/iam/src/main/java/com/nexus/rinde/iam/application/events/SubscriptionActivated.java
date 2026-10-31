package com.nexus.rinde.iam.application.events;

import com.nexus.rinde.shared.domain.model.events.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;

/** Lo publica Subscriptions al activar una suscripción; en S2-09 llegará por RabbitMQ. */
public record SubscriptionActivated(UUID eventId, Instant occurredAt, UUID tenantId)
    implements IntegrationEvent {

  @Override
  public String type() {
    return "SubscriptionActivated";
  }
}
