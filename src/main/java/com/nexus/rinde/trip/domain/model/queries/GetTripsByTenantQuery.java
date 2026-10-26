package com.nexus.rinde.trip.domain.model.queries;

import com.nexus.rinde.trip.domain.model.valueobjects.TripStatus;
import java.util.Set;
import java.util.UUID;

/** Consulta el tablero de una empresa, opcionalmente filtrado por estados. */
public record GetTripsByTenantQuery(UUID tenantId, Set<TripStatus> statuses) {}
