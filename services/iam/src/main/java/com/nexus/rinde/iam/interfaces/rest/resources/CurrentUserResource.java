package com.nexus.rinde.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Perfil del usuario que inicia sesión, con los datos de su empresa; nunca incluye contraseña. */
public record CurrentUserResource(
    @Schema(example = "7b0e4f52-1c9a-4f3e-8d21-5a6c9e0b3d44") UUID id,
    @Schema(example = "Ana Torres Vega") String fullName,
    @Schema(example = "ana.torres@transportesandes.pe") String email,
    @Schema(
            example = "ADMINISTRATOR",
            allowableValues = {"ADMINISTRATOR", "OPERATIONS_MANAGER", "DRIVER"})
        String role,
    @Schema(
            example = "ACTIVE",
            allowableValues = {"INVITED", "ACTIVE", "DISABLED"})
        String status,
    TenantResource tenant) {}
