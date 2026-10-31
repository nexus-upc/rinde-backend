package com.nexus.rinde.iam.domain.model.commands;

import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;

/** Reactiva la empresa porque su suscripción volvió a estar activa. */
public record ReactivateTenantCommand(TenantId tenantId) {}
