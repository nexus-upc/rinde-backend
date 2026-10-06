package com.nexus.rinde.trip.domain.model.events;

import com.nexus.rinde.shared.domain.model.events.IntegrationEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Anuncia una asignación para que Notifications informe al conductor. */
public record TripAssigned(
    UUID eventId,
    Instant occurredAt,
    UUID tenantId,
    UUID tripId,
    String code,
    String destination,
    LocalDate departureDate,
    UUID vehicleId,
    UUID driverId)
    implements IntegrationEvent {

  @Override
  public String type() {
    return "TripAssigned";
  }
}
