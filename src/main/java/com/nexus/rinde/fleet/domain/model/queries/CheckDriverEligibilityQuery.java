package com.nexus.rinde.fleet.domain.model.queries;

import java.util.UUID;

/** Consulta para verificar si un conductor está habilitado para ser asignado. */
public record CheckDriverEligibilityQuery(UUID tenantId, UUID driverId) {}
