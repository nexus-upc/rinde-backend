package com.nexus.rinde.fleet.domain.model.valueobjects;

import java.time.LocalDate;
import java.util.UUID;

/** Estado consolidado de salud técnica y mantenimiento de una unidad. */
public record VehicleHealthStatus(
    UUID vehicleId,
    String plateNumber,
    VehicleStatus vehicleStatus,
    MaintenanceState maintenanceState,
    LocalDate nextMaintenanceDate,
    boolean availableForTrip) {}
