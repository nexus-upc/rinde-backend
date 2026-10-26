package com.nexus.rinde.trip.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/** Resultado de asignar el vehículo y el conductor a un viaje. */
public record TripAssignmentResponseResource(
    @Schema(example = "7b0e4f52-1c9a-4f3e-8d21-5a6c9e0b3d44") UUID id,
    @Schema(example = "TRP-000001") String code,
    @Schema(example = "ASSIGNED") String status,
    @Schema(example = "deac3d8e-12d4-4a72-98f2-1e2fc4d0d230") UUID vehicleId,
    @Schema(example = "1355b6b8-7cd5-4f4f-9f66-7b883c7e8819") UUID driverId,
    @Schema(example = "2026-10-06T13:15:00Z") Instant assignedAt,
    @Schema(example = "DUE_SOON", allowableValues = {"DUE_SOON"}, nullable = true)
        String maintenanceAlert) {}
