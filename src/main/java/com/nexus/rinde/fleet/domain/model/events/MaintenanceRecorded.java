package com.nexus.rinde.fleet.domain.model.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Evento emitido cuando se registra un mantenimiento a una unidad. */
public record MaintenanceRecorded(
    UUID maintenanceId,
    UUID tenantId,
    UUID vehicleId,
    LocalDate nextMaintenanceDate,
    Instant occurredOn) {

  public MaintenanceRecorded(
      UUID maintenanceId, UUID tenantId, UUID vehicleId, LocalDate nextMaintenanceDate) {
    this(maintenanceId, tenantId, vehicleId, nextMaintenanceDate, Instant.now());
  }
}
