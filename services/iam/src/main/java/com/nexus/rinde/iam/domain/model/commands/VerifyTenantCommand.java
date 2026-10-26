package com.nexus.rinde.iam.domain.model.commands;

import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;

/** Confirma el correo de la empresa con el enlace de verificación. */
public record VerifyTenantCommand(TenantId tenantId, String token) {}
