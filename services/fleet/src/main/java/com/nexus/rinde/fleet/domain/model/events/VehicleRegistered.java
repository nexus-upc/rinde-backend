package com.nexus.rinde.fleet.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/** Evento emitido cuando se registra un vehículo en la flota. */
public record VehicleRegistered(
    UUID vehicleId,
    UUID tenantId,
    String plateNumber,
    Instant occurredOn) {

  public VehicleRegistered(UUID vehicleId, UUID tenantId, String plateNumber) {
    this(vehicleId, tenantId, plateNumber, Instant.now());
  }
}
