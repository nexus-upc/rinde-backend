package com.nexus.rinde.fleet.interfaces.rest.resources;

import com.nexus.rinde.fleet.domain.model.valueobjects.MaintenanceState;
import java.time.LocalDate;
import java.util.UUID;

/** Recurso de salida para alertas de mantenimiento (US34). */
public record MaintenanceAlertResource(
    UUID vehicleId,
    String plateNumber,
    MaintenanceState state,
    LocalDate nextMaintenanceDate,
    String message) {}
