package com.nexus.rinde.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/** Token de acceso que se envía luego en el encabezado Authorization: Bearer. */
public record AuthenticationResource(
    @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzZjZjMWI5ZSJ9.firma") String accessToken,
    @Schema(example = "Bearer") String tokenType,
    @Schema(example = "28800", description = "Segundos de vigencia del token")
        long expiresInSeconds) {}
