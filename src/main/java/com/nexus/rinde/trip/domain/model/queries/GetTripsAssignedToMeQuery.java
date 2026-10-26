package com.nexus.rinde.trip.domain.model.queries;

import java.util.UUID;

/** Consulta los viajes del conductor asociado al usuario autenticado. */
public record GetTripsAssignedToMeQuery(UUID tenantId, UUID userId) {}
