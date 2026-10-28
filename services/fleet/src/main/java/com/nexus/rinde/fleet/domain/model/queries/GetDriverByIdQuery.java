package com.nexus.rinde.fleet.domain.model.queries;

import java.util.UUID;

/** Consulta para obtener un conductor por su identificador. */
public record GetDriverByIdQuery(UUID tenantId, UUID driverId) {}
