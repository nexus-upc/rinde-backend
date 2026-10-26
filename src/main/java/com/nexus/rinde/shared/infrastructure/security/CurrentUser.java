package com.nexus.rinde.shared.infrastructure.security;

import java.util.UUID;

/** Datos del usuario autenticado tal como vienen en el token JWT. */
public record CurrentUser(
    UUID userId, UUID tenantId, String role, String tenantStatus, String email) {}
