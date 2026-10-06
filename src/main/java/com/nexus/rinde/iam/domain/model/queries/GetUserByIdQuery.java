package com.nexus.rinde.iam.domain.model.queries;

import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.model.valueobjects.UserId;

/** Busca un usuario dentro de una empresa. */
public record GetUserByIdQuery(TenantId tenantId, UserId userId) {}
