package com.nexus.rinde.fleet.domain.model.queries;

import java.util.UUID;

/** Consulta para listar los conductores de la empresa. */
public record ListDriversQuery(UUID tenantId) {}
