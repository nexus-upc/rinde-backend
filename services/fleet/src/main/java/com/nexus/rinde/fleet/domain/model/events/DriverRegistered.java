package com.nexus.rinde.fleet.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/** Evento emitido cuando se registra un conductor en la flota. */
public record DriverRegistered(
    UUID driverId,
    UUID tenantId,
    String licenseNumber,
    Instant occurredOn) {

  public DriverRegistered(UUID driverId, UUID tenantId, String licenseNumber) {
    this(driverId, tenantId, licenseNumber, Instant.now());
  }
}
