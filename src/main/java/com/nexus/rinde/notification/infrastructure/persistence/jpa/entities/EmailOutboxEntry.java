package com.nexus.rinde.notification.infrastructure.persistence.jpa.entities;

import com.nexus.rinde.notification.domain.model.valueobjects.EmailMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Registro idempotente del correo simulado, empleado como evidencia y cola operativa. */
@Entity
@Table(schema = "notification", name = "email_outbox")
public class EmailOutboxEntry {

  @Id
  @Column(name = "event_id", nullable = false, updatable = false)
  private UUID eventId;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private UUID tenantId;

  @Column(name = "recipient_email", nullable = false, length = 150, updatable = false)
  private String recipientEmail;

  @Column(name = "subject", nullable = false, length = 160, updatable = false)
  private String subject;

  @Column(name = "body", nullable = false, length = 1000, updatable = false)
  private String body;

  @Column(name = "delivery_status", nullable = false, length = 30)
  private String deliveryStatus;

  @Column(name = "delivery_attempts", nullable = false)
  private int deliveryAttempts;

  @Column(name = "last_error", length = 500)
  private String lastError;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "delivered_at")
  private Instant deliveredAt;

  protected EmailOutboxEntry() {}

  private EmailOutboxEntry(EmailMessage message, Instant now) {
    eventId = message.eventId();
    tenantId = message.tenantId();
    recipientEmail = message.recipient();
    subject = message.subject();
    body = message.body();
    deliveryStatus = "PENDING";
    deliveryAttempts = 0;
    createdAt = now;
  }

  public static EmailOutboxEntry pending(EmailMessage message, Instant now) {
    return new EmailOutboxEntry(message, now);
  }

  public void markSimulatedSent(Instant now) {
    deliveryStatus = "SIMULATED_SENT";
    deliveryAttempts++;
    lastError = null;
    deliveredAt = now;
  }

  public void markPending(String error) {
    deliveryStatus = "PENDING";
    deliveryAttempts++;
    lastError = error;
  }

  public UUID getEventId() {
    return eventId;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public String getRecipientEmail() {
    return recipientEmail;
  }

  public String getSubject() {
    return subject;
  }

  public String getBody() {
    return body;
  }

  public String getDeliveryStatus() {
    return deliveryStatus;
  }

  public int getDeliveryAttempts() {
    return deliveryAttempts;
  }

  public String getLastError() {
    return lastError;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getDeliveredAt() {
    return deliveredAt;
  }
}
