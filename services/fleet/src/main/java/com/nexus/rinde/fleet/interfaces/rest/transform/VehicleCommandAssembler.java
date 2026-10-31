package com.nexus.rinde.fleet.interfaces.rest.transform;

import com.nexus.rinde.fleet.domain.model.commands.RegisterVehicleCommand;
import com.nexus.rinde.fleet.interfaces.rest.resources.CreateVehicleResource;
import java.util.UUID;

/** Transformador de recurso REST a comando de creación de Vehicle. */
public final class VehicleCommandAssembler {

  private VehicleCommandAssembler() {}

  public static RegisterVehicleCommand toCommand(UUID tenantId, CreateVehicleResource resource) {
    return new RegisterVehicleCommand(
        tenantId,
        resource.plateNumber(),
        resource.brand(),
        resource.model(),
        resource.modelYear(),
        resource.payloadCapacityKg());
  }
}
