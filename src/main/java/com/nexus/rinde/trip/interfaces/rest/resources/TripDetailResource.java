package com.nexus.rinde.trip.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Detalle de un viaje con la carga, las fechas y el historial de estados. */
public record TripDetailResource(
    @Schema(example = "7b0e4f52-1c9a-4f3e-8d21-5a6c9e0b3d44") UUID id,
    @Schema(example = "TRP-000001") String code,
    @Schema(example = "Lima") String origin,
    @Schema(example = "Arequipa") String destination,
    @Schema(example = "Repuestos para maquinaria") String cargoDescription,
    @Schema(example = "1250.50", nullable = true) BigDecimal cargoWeightKg,
    @Schema(example = "2026-10-20") LocalDate departureDate,
    @Schema(example = "ASSIGNED") String status,
    @Schema(example = "deac3d8e-12d4-4a72-98f2-1e2fc4d0d230", nullable = true)
        UUID vehicleId,
    @Schema(example = "1355b6b8-7cd5-4f4f-9f66-7b883c7e8819", nullable = true)
        UUID driverId,
    @Schema(example = "2026-10-06T13:15:00Z", nullable = true) Instant assignedAt,
    @Schema(example = "9db235a4-3e8a-4c1b-8c65-a687c73b6a18", nullable = true)
        UUID overdueMaintenanceConfirmedBy,
    @Schema(example = "2026-10-20T08:00:00Z", nullable = true) Instant startedAt,
    @Schema(example = "2026-10-20T17:30:00Z", nullable = true) Instant finishedAt,
    List<TripStatusChangeResource> statusChanges) {}
