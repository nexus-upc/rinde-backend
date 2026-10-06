package com.nexus.rinde.iam.domain.model.queries;

import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;

/** Lista los usuarios de una empresa. */
public record GetUsersByTenantQuery(TenantId tenantId) {}
