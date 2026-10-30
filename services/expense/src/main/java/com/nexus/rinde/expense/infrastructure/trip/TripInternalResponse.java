package com.nexus.rinde.expense.infrastructure.trip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resumen del viaje obtenido desde el microservicio Trip (GET /api/internal/v1/trips/tenants/{tenantId}/{tripId}).
 * DTO propio del microservicio Expense sin depender del paquete ni clases de Trip.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TripInternalResponse(
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
    Instant finishedAt) {}
