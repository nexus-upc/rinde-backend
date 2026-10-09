package com.nexus.rinde.fleet.domain.model.commands;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Comando para registrar un mantenimiento técnico a una unidad. */
public record RecordMaintenanceCommand(
    UUID tenantId,
    UUID vehicleId,
    String maintenanceType,
    LocalDate executionDate,
    Integer mileage,
    BigDecimal cost,
    LocalDate nextMaintenanceDate,
    String notes) {}
