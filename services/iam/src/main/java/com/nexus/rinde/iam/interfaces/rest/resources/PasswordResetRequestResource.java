package com.nexus.rinde.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Correo del usuario que olvidó su contraseña. */
public record PasswordResetRequestResource(
    @Schema(example = "ana.rojas@transportesandes.pe") @NotBlank @Size(max = 150) String email) {}
