package com.nexus.rinde.iam.domain.model.queries;

import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;

/** Busca una empresa por su id. */
public record GetTenantByIdQuery(TenantId tenantId) {}
