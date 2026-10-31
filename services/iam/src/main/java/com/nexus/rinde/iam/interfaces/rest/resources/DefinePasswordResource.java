package com.nexus.rinde.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Enlace de acceso (invitación o recuperación) y la contraseña nueva. TODO: política de contraseñas
 * no definida en el diseño; se asumió de 8 a 72 caracteres.
 */
public record DefinePasswordResource(
    @Schema(example = "Qx7f0mK2p4n9sT1uVb3yZ6aCdEhJkLw8RtYiOpAsDfG") @NotBlank String token,
    @Schema(example = "Nueva-Clave-2026", description = "Entre 8 y 72 caracteres")
        @NotBlank
        @Size(min = 8, max = 72)
        String password) {}
