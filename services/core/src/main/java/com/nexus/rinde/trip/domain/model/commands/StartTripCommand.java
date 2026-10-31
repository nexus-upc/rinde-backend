package com.nexus.rinde.trip.domain.model.commands;

import java.util.UUID;

/** Solicitud autenticada del conductor para iniciar su viaje. */
public record StartTripCommand(UUID tenantId, UUID tripId, UUID userId) {}
