package com.nexus.rinde.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Usuario tal como lo devuelve la API; nunca incluye contraseña ni enlaces. */
public record UserResource(
    @Schema(example = "7b0e4f52-1c9a-4f3e-8d21-5a6c9e0b3d44") UUID id,
    @Schema(example = "Luis Quispe Mamani") String fullName,
    @Schema(example = "luis.quispe@transportesandes.pe") String email,
    @Schema(
            example = "DRIVER",
            allowableValues = {"ADMINISTRATOR", "OPERATIONS_MANAGER", "DRIVER"})
        String role,
    @Schema(
            example = "INVITED",
            allowableValues = {"INVITED", "ACTIVE", "DISABLED"})
        String status) {}
