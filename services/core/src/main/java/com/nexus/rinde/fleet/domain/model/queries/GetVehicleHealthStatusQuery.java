package com.nexus.rinde.fleet.domain.model.queries;

import java.util.UUID;

/** Consulta para obtener el estado de salud y mantenimiento de una unidad. */
public record GetVehicleHealthStatusQuery(UUID tenantId, UUID vehicleId) {}
