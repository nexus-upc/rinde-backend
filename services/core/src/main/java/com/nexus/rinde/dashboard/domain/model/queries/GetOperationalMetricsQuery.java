package com.nexus.rinde.dashboard.domain.model.queries;

import java.util.UUID;

/** Consulta las métricas operativas consolidadas de la empresa (US37). */
public record GetOperationalMetricsQuery(UUID tenantId) {}
