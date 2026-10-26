package com.nexus.rinde.iam.interfaces.internal.resources;

import java.util.UUID;

/** Datos de un usuario de la empresa para los demás servicios. */
public record InternalUserResource(
    UUID userId, UUID tenantId, String fullName, String email, String role, String status) {}
