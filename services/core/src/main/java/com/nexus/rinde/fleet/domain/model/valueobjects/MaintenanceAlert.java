package com.nexus.rinde.fleet.domain.model.valueobjects;

import java.time.LocalDate;
import java.util.UUID;

/** Alerta de mantenimiento para una unidad. */
public record MaintenanceAlert(
    UUID vehicleId,
    String plateNumber,
    MaintenanceState state,
    LocalDate nextMaintenanceDate,
    String message) {}
