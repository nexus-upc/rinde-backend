package com.nexus.rinde.trip.interfaces.internal.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Resumen de un viaje para otros servicios. El estado viaja como nombre del enum. */
public record InternalTripResource(
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
