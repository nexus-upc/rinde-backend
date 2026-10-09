package com.nexus.rinde.fleet.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Datos de entrada para registrar un vehículo en la flota (US12). */
public record CreateVehicleResource(
    @NotBlank(message = "La placa es obligatoria.") String plateNumber,
    @NotBlank(message = "La marca es obligatoria.") String brand,
    @NotBlank(message = "El modelo es obligatorio.") String model,
    @NotNull(message = "El año del modelo es obligatorio.") Integer modelYear,
    @NotNull(message = "La capacidad de carga es obligatoria.")
        @Positive(message = "La capacidad de carga debe ser positiva.")
        BigDecimal payloadCapacityKg) {}
