package com.nexus.rinde.notification.application.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Contrato propio de Notifications para recibir trip.assigned sin importar el dominio de Trip. */
public record TripAssignedNotification(
    UUID eventId, Instant occurredAt, UUID tenantId, UUID tripId, String code,
    String destination, LocalDate departureDate, UUID vehicleId, UUID driverId) {

  public TripAssignedNotification {
    Objects.requireNonNull(eventId, "Falta el identificador del evento.");
    Objects.requireNonNull(occurredAt, "Falta la fecha del evento.");
    Objects.requireNonNull(tenantId, "Falta la empresa.");
    Objects.requireNonNull(tripId, "Falta el viaje.");
    Objects.requireNonNull(driverId, "Falta el conductor.");
    Objects.requireNonNull(vehicleId, "Falta el vehículo.");
    Objects.requireNonNull(departureDate, "Falta la fecha de salida.");
    if (code == null || code.isBlank() || code.length() > 20
        || destination == null || destination.isBlank() || destination.length() > 120) {
      throw new IllegalArgumentException("El código o el destino del viaje no son válidos.");
    }
  }
}
