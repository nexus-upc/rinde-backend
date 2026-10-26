package com.nexus.rinde.trip.domain.model.events;

import com.nexus.rinde.shared.domain.model.events.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;

/** Anuncia la finalización para habilitar Settlement y liberar la unidad. */
public record TripFinished(
    UUID eventId,
    Instant occurredAt,
    UUID tenantId,
    UUID tripId,
    UUID vehicleId,
    UUID driverId)
    implements IntegrationEvent {

  @Override
  public String type() {
    return "TripFinished";
  }
}
