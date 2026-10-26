package com.nexus.rinde.trip.interfaces.acl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Vista de un viaje para otros contextos. El estado viaja como nombre del enum. */
public record TripSummary(
    UUID id,
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
