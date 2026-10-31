package com.nexus.rinde.fleet.domain.model.queries;

import java.util.UUID;

/** Consulta para obtener un vehículo por identificador. */
public record GetVehicleByIdQuery(UUID tenantId, UUID vehicleId) {}
