package com.nexus.rinde.iam.interfaces.acl;

import java.util.UUID;

/** Datos simples de un usuario para otros contextos; nunca expone la entidad de IAM. */
public record UserSummary(
    UUID userId, UUID tenantId, String fullName, String email, String role, String status) {}
