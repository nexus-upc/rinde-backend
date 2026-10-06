package com.nexus.rinde.trip.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/** Elemento del historial de estados. */
public record TripStatusChangeResource(
    @Schema(example = "ASSIGNED") String status,
    @Schema(example = "2026-10-06T13:15:00Z") Instant changedAt,
    @Schema(example = "9db235a4-3e8a-4c1b-8c65-a687c73b6a18", nullable = true)
        UUID changedBy) {}
