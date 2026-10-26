package com.nexus.rinde.trip.domain.model.commands;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Datos para programar un viaje en la empresa del usuario autenticado. */
public record ScheduleTripCommand(
    UUID tenantId,
    UUID userId,
    String origin,
    String destination,
    String cargoDescription,
    BigDecimal cargoWeightKg,
    LocalDate departureDate,
    boolean confirmPastDate) {}
