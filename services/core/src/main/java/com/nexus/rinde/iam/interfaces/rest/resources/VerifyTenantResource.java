package com.nexus.rinde.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Token de verificación que recibió el administrador. */
public record VerifyTenantResource(
    @Schema(example = "Qx7f0mK2p4n9sT1uVb3yZ6aCdEhJkLw8RtYiOpAsDfG") @NotBlank String token) {}
