package com.nexus.rinde.notification.infrastructure.persistence.jpa.entities;

import com.nexus.rinde.notification.domain.model.valueobjects.TripAssignmentNotice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Registro operativo de un aviso pendiente o entregado, con eventId como clave idempotente. */
@Entity
@Table(schema = "notification", name = "retry_store")
public class RetryStoreEntry {

  @Id
  @Column(name = "event_id", nullable = false, updatable = false)
  private UUID eventId;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private UUID tenantId;

  @Column(name = "trip_id", nullable = false, updatable = false)
  private UUID tripId;

  @Column(name = "recipient_driver_id", nullable = false, updatable = false)
  private UUID recipientDriverId;

  @Column(name = "trip_code", nullable = false, length = 20, updatable = false)
  private String tripCode;

  @Column(name = "destination", nullable = false, length = 120, updatable = false)
  private String destination;

  @Column(name = "departure_date", nullable = false, updatable = false)
  private LocalDate departureDate;

  @Column(name = "message", nullable = false, length = 500, updatable = false)
  private String message;

  @Column(name = "delivery_status", nullable = false, length = 20)
  private String deliveryStatus;

  @Column(name = "retry_count", nullable = false)
  private int retryCount;

  @Column(name = "last_error", length = 500)
  private String lastError;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "delivered_at")
  private Instant deliveredAt;

  protected RetryStoreEntry() {}

  private RetryStoreEntry(TripAssignmentNotice notice, Instant now) {
    this.eventId = notice.eventId();
    this.tenantId = notice.tenantId();
    this.tripId = notice.tripId();
    this.recipientDriverId = notice.driverId();
    this.tripCode = notice.tripCode();
    this.destination = notice.destination();
    this.departureDate = notice.departureDate();
    this.message = notice.message();
    this.deliveryStatus = "PENDING";
    this.retryCount = 0;
    this.lastError = "No hay token de dispositivo ni proveedor push configurado.";
    this.createdAt = now;
    this.updatedAt = now;
  }

  public static RetryStoreEntry prepared(TripAssignmentNotice notice, Instant now) {
    return new RetryStoreEntry(notice, now);
  }

  public void markDelivered(Instant now) {
    this.deliveryStatus = "DELIVERED";
    this.lastError = null;
    this.deliveredAt = now;
    this.updatedAt = now;
  }

  public void markPending(String error, boolean deliveryAttempted, Instant now) {
    this.deliveryStatus = "PENDING";
    this.lastError = error;
    if (deliveryAttempted) {
      this.retryCount++;
    }
    this.updatedAt = now;
  }

  public UUID getEventId() {
    return eventId;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public UUID getTripId() {
    return tripId;
  }

  public UUID getRecipientDriverId() {
    return recipientDriverId;
  }

  public String getTripCode() {
    return tripCode;
  }

  public String getDestination() {
    return destination;
  }

  public LocalDate getDepartureDate() {
    return departureDate;
  }

  public String getMessage() {
    return message;
  }

  public String getDeliveryStatus() {
    return deliveryStatus;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public String getLastError() {
    return lastError;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getDeliveredAt() {
    return deliveredAt;
  }
}
