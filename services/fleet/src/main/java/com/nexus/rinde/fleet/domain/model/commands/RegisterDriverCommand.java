package com.nexus.rinde.fleet.domain.model.commands;

import java.time.LocalDate;
import java.util.UUID;

/** Comando para registrar un conductor en la flota. */
public record RegisterDriverCommand(
    UUID tenantId,
    UUID userId,
    String fullName,
    String documentType,
    String documentNumber,
    String licenseNumber,
    String licenseCategory,
    LocalDate licenseExpirationDate) {}
