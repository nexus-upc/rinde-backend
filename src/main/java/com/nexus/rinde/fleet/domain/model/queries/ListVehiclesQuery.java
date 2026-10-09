package com.nexus.rinde.fleet.domain.model.queries;

import java.util.UUID;

/** Consulta para listar los vehículos de la empresa. */
public record ListVehiclesQuery(UUID tenantId) {}
