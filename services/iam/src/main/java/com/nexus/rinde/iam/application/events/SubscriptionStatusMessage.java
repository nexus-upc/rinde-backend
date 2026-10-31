package com.nexus.rinde.iam.application.events;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/** Payload propio de IAM; la routing key distingue activación y suspensión. */
public record SubscriptionStatusMessage(UUID eventId, Instant occurredAt, UUID tenantId) {
  public SubscriptionStatusMessage {
    Objects.requireNonNull(eventId, "Falta el identificador del evento.");
    Objects.requireNonNull(tenantId, "Falta la empresa.");
    occurredAt = Objects.requireNonNull(occurredAt, "Falta la fecha del evento.")
        .truncatedTo(ChronoUnit.MICROS);
  }
}
