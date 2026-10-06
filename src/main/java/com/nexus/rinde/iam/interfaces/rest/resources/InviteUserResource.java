package com.nexus.rinde.iam.interfaces.rest.resources;

import com.nexus.rinde.iam.domain.model.valueobjects.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para invitar a un usuario a la empresa. */
public record InviteUserResource(
    @Schema(example = "Luis Quispe Mamani") @NotBlank @Size(max = 150) String fullName,
    @Schema(example = "luis.quispe@transportesandes.pe") @NotBlank @Email @Size(max = 150)
        String email,
    @Schema(example = "DRIVER") @NotNull Role role) {}
