package com.nexus.rinde.fleet.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Recurso de salida para un mantenimiento registrado. */
public record MaintenanceResource(
    UUID id,
    UUID vehicleId,
    String maintenanceType,
    LocalDate executionDate,
    Integer mileage,
    BigDecimal cost,
    LocalDate nextMaintenanceDate,
    String notes,
    Instant createdAt) {}
