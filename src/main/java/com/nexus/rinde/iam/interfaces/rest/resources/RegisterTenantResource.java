package com.nexus.rinde.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para registrar una empresa y a su administrador. TODO: el diseño no define la política de
 * contraseñas; se asumió de 8 a 72 caracteres (72 es el límite de BCrypt).
 */
public record RegisterTenantResource(
    @Schema(example = "Transportes Andes SAC") @NotBlank @Size(max = 120) String tradeName,
    @Schema(example = "20123456789")
        @NotBlank
        @Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dígitos")
        String ruc,
    @Schema(example = "Ana Rojas Paredes") @NotBlank @Size(max = 150) String administratorFullName,
    @Schema(example = "ana.rojas@transportesandes.pe") @NotBlank @Email @Size(max = 150)
        String administratorEmail,
    @Schema(example = "Clave-Segura-2026") @NotBlank @Size(min = 8, max = 72) String password) {}
