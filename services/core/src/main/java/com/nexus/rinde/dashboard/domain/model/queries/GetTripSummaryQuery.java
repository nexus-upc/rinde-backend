package com.nexus.rinde.dashboard.domain.model.queries;

import java.util.UUID;

/** Consulta el resumen consolidado de un viaje para el dashboard (US17). */
public record GetTripSummaryQuery(UUID tenantId, UUID tripId) {}
