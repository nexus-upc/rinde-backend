package com.nexus.rinde.dashboard.domain.model.queries;

import java.util.UUID;

/** Consulta el estado actual de la flota de la empresa (US37). */
public record GetFleetStatusQuery(UUID tenantId) {}
