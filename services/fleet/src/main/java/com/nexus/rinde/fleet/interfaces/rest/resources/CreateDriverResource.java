package com.nexus.rinde.fleet.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/** Datos de entrada para registrar un conductor con su licencia (US13). */
public record CreateDriverResource(
    UUID userId,
    @NotBlank(message = "Los nombres del conductor son obligatorios.") String fullName,
    @NotBlank(message = "El tipo de documento es obligatorio.") String documentType,
    @NotBlank(message = "El número de documento es obligatorio.") String documentNumber,
    @NotBlank(message = "El número de licencia es obligatorio.") String licenseNumber,
    @NotBlank(message = "La categoría de licencia es obligatoria.") String licenseCategory,
    @NotNull(message = "La fecha de vencimiento es obligatoria.")
        LocalDate licenseExpirationDate) {}
