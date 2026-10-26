package com.nexus.rinde.fleet.domain.model.queries;

import java.util.UUID;

/** Consulta para listar las alertas de mantenimiento pendientes de la flota. */
public record ListMaintenanceAlertsQuery(UUID tenantId) {}
