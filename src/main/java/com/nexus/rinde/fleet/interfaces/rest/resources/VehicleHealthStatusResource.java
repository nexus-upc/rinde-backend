package com.nexus.rinde.fleet.interfaces.rest.resources;

import com.nexus.rinde.fleet.domain.model.valueobjects.MaintenanceState;
import com.nexus.rinde.fleet.domain.model.valueobjects.VehicleStatus;
import java.time.LocalDate;
import java.util.UUID;

/** Recurso de salida para la salud técnica y mantenimiento del vehículo (US15, US34). */
public record VehicleHealthStatusResource(
    UUID vehicleId,
    String plateNumber,
    VehicleStatus vehicleStatus,
    MaintenanceState maintenanceState,
    LocalDate nextMaintenanceDate,
    boolean availableForTrip) {}
