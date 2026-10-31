package com.nexus.rinde.fleet.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Datos de entrada para registrar un mantenimiento técnico (US33). */
public record RecordMaintenanceResource(
    UUID vehicleId,
    @NotBlank(message = "El tipo de mantenimiento es obligatorio.") String maintenanceType,
    @NotNull(message = "La fecha de ejecución es obligatoria.") LocalDate executionDate,
    Integer mileage,
    @NotNull(message = "El costo es obligatorio.")
        @PositiveOrZero(message = "El costo no puede ser negativo.")
        BigDecimal cost,
    @NotNull(message = "La fecha del próximo mantenimiento es obligatoria.")
        LocalDate nextMaintenanceDate,
    String notes) {}
