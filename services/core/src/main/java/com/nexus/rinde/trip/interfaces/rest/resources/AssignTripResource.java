package com.nexus.rinde.trip.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Identificadores Fleet de la unidad y el conductor, más la confirmación de mantenimiento. */
public record AssignTripResource(
    @Schema(example = "deac3d8e-12d4-4a72-98f2-1e2fc4d0d230") @NotNull UUID vehicleId,
    @Schema(example = "1355b6b8-7cd5-4f4f-9f66-7b883c7e8819") @NotNull UUID driverId,
    @Schema(example = "false") boolean confirmOverdueMaintenance) {}
