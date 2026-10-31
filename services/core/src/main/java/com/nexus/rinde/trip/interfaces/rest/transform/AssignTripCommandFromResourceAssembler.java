package com.nexus.rinde.trip.interfaces.rest.transform;

import com.nexus.rinde.trip.domain.model.commands.AssignTripCommand;
import com.nexus.rinde.trip.interfaces.rest.resources.AssignTripResource;
import java.util.UUID;

/** Convierte el request de asignación en el comando del caso de uso. */
public final class AssignTripCommandFromResourceAssembler {

  private AssignTripCommandFromResourceAssembler() {}

  public static AssignTripCommand toCommand(
      UUID tenantId, UUID tripId, UUID assignedBy, AssignTripResource resource) {
    return new AssignTripCommand(
        tenantId,
        tripId,
        resource.vehicleId(),
        resource.driverId(),
        assignedBy,
        resource.confirmOverdueMaintenance());
  }
}
