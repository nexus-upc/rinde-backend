package com.nexus.rinde.iam.interfaces.internal.resources;

import java.util.UUID;

/** Estado actual de una empresa para los demás servicios. */
public record TenantStatusResource(UUID tenantId, String status) {}
