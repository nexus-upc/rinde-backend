package com.nexus.rinde.notification.domain.model.valueobjects;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Aviso de asignación preparado a partir del evento público TripAssigned. */
public record TripAssignmentNotice(
    UUID eventId,
    UUID tenantId,
    UUID tripId,
    UUID driverId,
    String tripCode,
    String destination,
    LocalDate departureDate,
    Instant occurredAt,
    String message) {

  public static TripAssignmentNotice from(
      UUID eventId,
      UUID tenantId,
      UUID tripId,
      UUID driverId,
      String tripCode,
      String destination,
      LocalDate departureDate,
      Instant occurredAt) {
    String message =
        "Viaje "
            + tripCode
            + " asignado. Destino: "
            + destination
            + ". Fecha de salida: "
            + departureDate
            + ".";
    return new TripAssignmentNotice(
        eventId,
        tenantId,
        tripId,
        driverId,
        tripCode,
        destination,
        departureDate,
        occurredAt,
        message);
  }
}
