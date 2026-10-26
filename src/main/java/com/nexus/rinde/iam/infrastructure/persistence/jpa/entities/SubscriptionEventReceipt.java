package com.nexus.rinde.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Guarda el identificador y la fecha del evento en la misma transacción que el estado de la empresa. */
@Entity
@Table(schema = "iam", name = "subscription_event_receipts")
public class SubscriptionEventReceipt {

  @Id
  @Column(name = "event_id", nullable = false, updatable = false)
  private UUID eventId;
  @Column(name = "tenant_id", nullable = false, updatable = false)
  private UUID tenantId;
  @Column(name = "event_type", nullable = false, length = 40, updatable = false)
  private String eventType;
  @Column(name = "occurred_at", nullable = false, updatable = false)
  private Instant occurredAt;
  @Column(name = "processed_at", nullable = false, updatable = false)
  private Instant processedAt;

  protected SubscriptionEventReceipt() {}

  public SubscriptionEventReceipt(UUID eventId, UUID tenantId, String eventType,
      Instant occurredAt, Instant processedAt) {
    this.eventId = eventId;
    this.tenantId = tenantId;
    this.eventType = eventType;
    this.occurredAt = occurredAt;
    this.processedAt = processedAt;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }
}
