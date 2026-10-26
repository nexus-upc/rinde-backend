package com.nexus.rinde.dashboard.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Resumen consolidado de un viaje para el dashboard (US17). */
public record TripSummaryResource(
    UUID tripId,
    String code,
    String status,
    String origin,
    String destination,
    String cargoDescription,
    BigDecimal cargoWeightKg,
    LocalDate departureDate,
    UUID vehicleId,
    UUID driverId,
    Instant startedAt,
    Instant finishedAt,
    int expenseCount,
    BigDecimal expenseTotal,
    String expenseCurrency,
    String settlementStatus,
    BigDecimal advanceAmount,
    BigDecimal advanceBalance) {}
