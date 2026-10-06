package com.nexus.rinde.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Credenciales para iniciar sesión. */
public record SignInResource(
    @Schema(example = "ana.rojas@transportesandes.pe") @NotBlank @Size(max = 150) String email,
    @Schema(example = "Clave-Segura-2026") @NotBlank @Size(max = 72) String password) {}
