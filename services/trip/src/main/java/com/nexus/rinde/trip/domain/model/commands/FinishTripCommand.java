package com.nexus.rinde.trip.domain.model.commands;

import java.util.UUID;

/** Solicitud autenticada del conductor para finalizar su viaje. */
public record FinishTripCommand(UUID tenantId, UUID tripId, UUID userId) {}
