package com.nexus.rinde.trip.domain.model.events;

import com.nexus.rinde.shared.domain.model.events.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;

/** Anuncia el inicio de un viaje a los contextos que habilitan operaciones en ruta. */
public record TripStarted(
    UUID eventId,
    Instant occurredAt,
    UUID tenantId,
    UUID tripId,
    UUID vehicleId,
    UUID driverId)
    implements IntegrationEvent {

  @Override
  public String type() {
    return "TripStarted";
  }
}
