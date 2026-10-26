package com.nexus.rinde.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Empresa tal como la devuelve la API. */
public record TenantResource(
    @Schema(example = "3f6c1b9e-8a2d-4c57-9a43-0d2f6b1e7a10") UUID id,
    @Schema(example = "Transportes Andes SAC") String tradeName,
    @Schema(example = "20123456789") String ruc,
    @Schema(
            example = "PENDING_VERIFICATION",
            allowableValues = {"PENDING_VERIFICATION", "ACTIVE", "RESTRICTED"})
        String status) {}
