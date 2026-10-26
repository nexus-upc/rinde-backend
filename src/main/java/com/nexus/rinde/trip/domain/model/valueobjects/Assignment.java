package com.nexus.rinde.trip.domain.model.valueobjects;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import java.util.UUID;

/** Identificadores de los recursos asignados y su confirmación de mantenimiento vencido. */
@Embeddable
public record Assignment(
    @Column(name = "vehicle_id") UUID vehicleId,
    @Column(name = "driver_id") UUID driverId,
    @Column(name = "assigned_at") Instant assignedAt,
    @Column(name = "maintenance_confirmed_by") UUID overdueMaintenanceConfirmedBy) {

  public Assignment {
    if (vehicleId == null || driverId == null || assignedAt == null) {
      throw new BusinessRuleException(
          "El vehículo, el conductor y la fecha de asignación son obligatorios.");
    }
  }
}
