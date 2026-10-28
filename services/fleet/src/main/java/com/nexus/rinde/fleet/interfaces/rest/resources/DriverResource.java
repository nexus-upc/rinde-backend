package com.nexus.rinde.fleet.interfaces.rest.resources;

import com.nexus.rinde.fleet.domain.model.valueobjects.DriverStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Recurso de salida con los datos del conductor registrado. */
public record DriverResource(
    UUID id,
    UUID userId,
    String fullName,
    String documentType,
    String documentNumber,
    String licenseNumber,
    String licenseCategory,
    LocalDate licenseExpirationDate,
    DriverStatus status,
    Instant createdAt) {}
