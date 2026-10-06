package com.nexus.rinde.trip.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos para programar un viaje; la empresa siempre se toma del token. */
public record ScheduleTripResource(
    @Schema(example = "Lima") @NotBlank @Size(max = 120) String origin,
    @Schema(example = "Arequipa") @NotBlank @Size(max = 120) String destination,
    @Schema(example = "Repuestos para maquinaria") @NotBlank @Size(max = 200)
        String cargoDescription,
    @Schema(example = "1250.50", nullable = true)
        @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 8, fraction = 2)
        BigDecimal cargoWeightKg,
    @Schema(example = "2026-10-20") @NotNull LocalDate departureDate,
    @Schema(example = "false") boolean confirmPastDate) {}
