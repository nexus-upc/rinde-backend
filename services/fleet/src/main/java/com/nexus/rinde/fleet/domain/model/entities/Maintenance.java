package com.nexus.rinde.fleet.domain.model.entities;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Registro de mantenimiento preventivo o correctivo de una unidad. */
@Entity
@Table(schema = "fleet", name = "maintenances")
public class Maintenance {

  @Id private UUID id;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private UUID tenantId;

  @Column(name = "vehicle_id", nullable = false, updatable = false)
  private UUID vehicleId;

  @Column(name = "maintenance_type", nullable = false, length = 50)
  private String maintenanceType;

  @Column(name = "execution_date", nullable = false)
  private LocalDate executionDate;

  @Column(name = "mileage")
  private Integer mileage;

  @Column(name = "cost", nullable = false, precision = 10, scale = 2)
  private BigDecimal cost;

  @Column(name = "next_maintenance_date", nullable = false)
  private LocalDate nextMaintenanceDate;

  @Column(name = "notes", length = 500)
  private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Maintenance() {}

  public Maintenance(
      UUID tenantId,
      UUID vehicleId,
      String maintenanceType,
      LocalDate executionDate,
      Integer mileage,
      BigDecimal cost,
      LocalDate nextMaintenanceDate,
      String notes) {
    if (tenantId == null) {
      throw new BusinessRuleException("El tenantId es obligatorio.");
    }
    if (vehicleId == null) {
      throw new BusinessRuleException("El vehicleId es obligatorio.");
    }
    if (maintenanceType == null || maintenanceType.isBlank()) {
      throw new BusinessRuleException("El tipo de mantenimiento es obligatorio.");
    }
    if (executionDate == null) {
      throw new BusinessRuleException("La fecha de ejecución es obligatoria.");
    }
    if (cost == null || cost.compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessRuleException("El costo no puede ser negativo.");
    }
    if (nextMaintenanceDate == null) {
      throw new BusinessRuleException("La fecha del próximo mantenimiento es obligatoria.");
    }
    if (nextMaintenanceDate.isBefore(executionDate)) {
      throw new BusinessRuleException(
          "La fecha del próximo mantenimiento no puede ser anterior a la fecha de ejecución.");
    }

    this.id = UUID.randomUUID();
    this.tenantId = tenantId;
    this.vehicleId = vehicleId;
    this.maintenanceType = maintenanceType.trim().toUpperCase();
    this.executionDate = executionDate;
    this.mileage = mileage;
    this.cost = cost;
    this.nextMaintenanceDate = nextMaintenanceDate;
    this.notes = notes != null ? notes.trim() : null;
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public UUID getVehicleId() {
    return vehicleId;
  }

  public String getMaintenanceType() {
    return maintenanceType;
  }

  public LocalDate getExecutionDate() {
    return executionDate;
  }

  public Integer getMileage() {
    return mileage;
  }

  public BigDecimal getCost() {
    return cost;
  }

  public LocalDate getNextMaintenanceDate() {
    return nextMaintenanceDate;
  }

  public String getNotes() {
    return notes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
