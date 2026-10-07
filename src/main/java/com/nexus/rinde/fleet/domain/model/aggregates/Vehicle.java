package com.nexus.rinde.fleet.domain.model.aggregates;

import com.nexus.rinde.fleet.domain.model.entities.Maintenance;
import com.nexus.rinde.fleet.domain.model.events.VehicleRegistered;
import com.nexus.rinde.fleet.domain.model.valueobjects.MaintenanceState;
import com.nexus.rinde.fleet.domain.model.valueobjects.VehicleHealthStatus;
import com.nexus.rinde.fleet.domain.model.valueobjects.VehicleStatus;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import com.nexus.rinde.shared.domain.exceptions.ConflictException;
import com.nexus.rinde.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Unidad de transporte y raíz del agregado Vehicle. */
@Entity
@Table(
    schema = "fleet",
    name = "vehicles",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_vehicles_tenant_plate",
            columnNames = {"tenant_id", "plate_number"}),
    indexes = {@Index(name = "idx_vehicles_tenant_status", columnList = "tenant_id, status")})
public class Vehicle extends AuditableAbstractAggregateRoot {

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private UUID tenantId;

  @Column(name = "plate_number", nullable = false, length = 10)
  private String plateNumber;

  @Column(name = "brand", nullable = false, length = 50)
  private String brand;

  @Column(name = "model", nullable = false, length = 50)
  private String model;

  @Column(name = "model_year", nullable = false)
  private Integer modelYear;

  @Column(name = "payload_capacity_kg", nullable = false, precision = 10, scale = 2)
  private BigDecimal payloadCapacityKg;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private VehicleStatus status;

  protected Vehicle() {}

  public Vehicle(
      UUID tenantId,
      String plateNumber,
      String brand,
      String model,
      Integer modelYear,
      BigDecimal payloadCapacityKg) {
    if (tenantId == null) {
      throw new BusinessRuleException("El identificador de empresa es obligatorio.");
    }
    if (plateNumber == null || plateNumber.trim().length() < 3) {
      throw new BusinessRuleException("La placa de rodaje no es válida.");
    }
    if (brand == null || brand.isBlank()) {
      throw new BusinessRuleException("La marca del vehículo es obligatoria.");
    }
    if (model == null || model.isBlank()) {
      throw new BusinessRuleException("El modelo del vehículo es obligatorio.");
    }
    if (modelYear == null || modelYear < 1950) {
      throw new BusinessRuleException("El año de fabricación no es válido.");
    }
    if (payloadCapacityKg == null || payloadCapacityKg.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessRuleException("La capacidad de carga debe ser mayor a cero.");
    }

    this.tenantId = tenantId;
    this.plateNumber = plateNumber.trim().toUpperCase();
    this.brand = brand.trim();
    this.model = model.trim();
    this.modelYear = modelYear;
    this.payloadCapacityKg = payloadCapacityKg;
    this.status = VehicleStatus.AVAILABLE;

    registerEvent(new VehicleRegistered(this.getId(), this.tenantId, this.plateNumber));
  }

  public void assignToTrip() {
    if (this.status != VehicleStatus.AVAILABLE) {
      throw new ConflictException(
          "El vehículo no se encuentra disponible (estado actual: " + this.status + ").");
    }
    this.status = VehicleStatus.IN_TRIP;
  }

  public void releaseFromTrip() {
    this.status = VehicleStatus.AVAILABLE;
  }

  public void sendToMaintenance() {
    this.status = VehicleStatus.IN_MAINTENANCE;
  }

  public void finishMaintenance() {
    this.status = VehicleStatus.AVAILABLE;
  }

  public VehicleHealthStatus evaluateHealth(LocalDate today, List<Maintenance> maintenances) {
    if (maintenances == null || maintenances.isEmpty()) {
      return new VehicleHealthStatus(
          this.getId(),
          this.plateNumber,
          this.status,
          MaintenanceState.UP_TO_DATE,
          null,
          this.status == VehicleStatus.AVAILABLE);
    }

    Maintenance latest =
        maintenances.stream()
            .max(Comparator.comparing(Maintenance::getNextMaintenanceDate))
            .orElse(maintenances.get(0));

    LocalDate nextDate = latest.getNextMaintenanceDate();
    MaintenanceState state;
    if (nextDate.isBefore(today)) {
      state = MaintenanceState.OVERDUE;
    } else if (!nextDate.isAfter(today.plusDays(7))) {
      state = MaintenanceState.DUE_SOON;
    } else {
      state = MaintenanceState.UP_TO_DATE;
    }

    boolean available = this.status == VehicleStatus.AVAILABLE && state != MaintenanceState.OVERDUE;

    return new VehicleHealthStatus(
        this.getId(), this.plateNumber, this.status, state, nextDate, available);
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public String getPlateNumber() {
    return plateNumber;
  }

  public String getBrand() {
    return brand;
  }

  public String getModel() {
    return model;
  }

  public Integer getModelYear() {
    return modelYear;
  }

  public BigDecimal getPayloadCapacityKg() {
    return payloadCapacityKg;
  }

  public VehicleStatus getStatus() {
    return status;
  }
}
