package com.nexus.rinde.trip.domain.model.queries;

import java.util.UUID;

/** Consulta un viaje visible para la empresa y el usuario que lo solicita. */
public record GetTripByIdQuery(UUID tenantId, UUID tripId, UUID viewerUserId, String viewerRole) {}
