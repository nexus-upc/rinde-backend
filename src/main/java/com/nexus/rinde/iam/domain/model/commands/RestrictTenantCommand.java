package com.nexus.rinde.iam.domain.model.commands;

import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;

/** Restringe la empresa porque su suscripción fue suspendida. */
public record RestrictTenantCommand(TenantId tenantId) {}
