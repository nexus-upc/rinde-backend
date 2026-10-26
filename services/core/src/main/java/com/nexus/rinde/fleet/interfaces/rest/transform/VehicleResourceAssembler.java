package com.nexus.rinde.fleet.interfaces.rest.transform;

import com.nexus.rinde.fleet.domain.model.aggregates.Vehicle;
import com.nexus.rinde.fleet.domain.model.valueobjects.VehicleHealthStatus;
import com.nexus.rinde.fleet.interfaces.rest.resources.VehicleHealthStatusResource;
import com.nexus.rinde.fleet.interfaces.rest.resources.VehicleResource;

/** Transformadores de objetos de dominio Vehicle a recursos REST. */
public final class VehicleResourceAssembler {

  private VehicleResourceAssembler() {}

  public static VehicleResource toResource(Vehicle entity) {
    return new VehicleResource(
        entity.getId(),
        entity.getPlateNumber(),
        entity.getBrand(),
        entity.getModel(),
        entity.getModelYear(),
        entity.getPayloadCapacityKg(),
        entity.getStatus(),
        entity.getCreatedAt());
  }

  public static VehicleHealthStatusResource toHealthStatusResource(VehicleHealthStatus status) {
    return new VehicleHealthStatusResource(
        status.vehicleId(),
        status.plateNumber(),
        status.vehicleStatus(),
        status.maintenanceState(),
        status.nextMaintenanceDate(),
        status.availableForTrip());
  }
}
