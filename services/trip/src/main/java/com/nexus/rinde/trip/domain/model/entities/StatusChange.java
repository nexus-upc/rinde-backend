package com.nexus.rinde.trip.domain.model.entities;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.domain.model.valueobjects.TripStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Cambio de estado que conserva quién lo realizó y cuándo ocurrió. */
@Entity
@Table(schema = "trip", name = "trip_status_changes")
public class StatusChange {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "trip_id", nullable = false, updatable = false)
  private Trip trip;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private UUID tenantId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private TripStatus status;

  @Column(name = "changed_at", nullable = false)
  private Instant changedAt;

  @Column(name = "changed_by", nullable = false)
  private UUID changedBy;

  protected StatusChange() {}

  public StatusChange(Trip trip, UUID tenantId, TripStatus status, Instant changedAt, UUID changedBy) {
    if (trip == null || tenantId == null || status == null || changedAt == null) {
      throw new BusinessRuleException("El cambio de estado está incompleto.");
    }
    if (changedBy == null) {
      throw new BusinessRuleException("El usuario que cambió el estado es obligatorio.");
    }
    this.id = UUID.randomUUID();
    this.trip = trip;
    this.tenantId = tenantId;
    this.status = status;
    this.changedAt = changedAt;
    this.changedBy = changedBy;
  }

  public TripStatus getStatus() {
    return status;
  }

  public Instant getChangedAt() {
    return changedAt;
  }

  public UUID getChangedBy() {
    return changedBy;
  }
}
