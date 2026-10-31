package com.nexus.rinde.trip.interfaces.rest.transform;

import com.nexus.rinde.trip.domain.model.commands.ScheduleTripCommand;
import com.nexus.rinde.trip.interfaces.rest.resources.ScheduleTripResource;
import java.util.UUID;

/** Convierte el request de programación en el comando del caso de uso. */
public final class ScheduleTripCommandFromResourceAssembler {

  private ScheduleTripCommandFromResourceAssembler() {}

  public static ScheduleTripCommand toCommand(
      UUID tenantId, UUID userId, ScheduleTripResource resource) {
    return new ScheduleTripCommand(
        tenantId,
        userId,
        resource.origin(),
        resource.destination(),
        resource.cargoDescription(),
        resource.cargoWeightKg(),
        resource.departureDate(),
        resource.confirmPastDate());
  }
}
