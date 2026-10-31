package com.nexus.rinde.trip.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

/** Resumen para el tablero de viajes. */
public record TripSummaryResource(
    @Schema(example = "7b0e4f52-1c9a-4f3e-8d21-5a6c9e0b3d44") UUID id,
    @Schema(example = "TRP-000001") String code,
    @Schema(example = "Lima") String origin,
    @Schema(example = "Arequipa") String destination,
    @Schema(example = "2026-10-20") LocalDate departureDate,
    @Schema(example = "deac3d8e-12d4-4a72-98f2-1e2fc4d0d230", nullable = true)
        UUID vehicleId,
    @Schema(example = "1355b6b8-7cd5-4f4f-9f66-7b883c7e8819", nullable = true)
        UUID driverId,
    @Schema(example = "SCHEDULED") String status) {}
