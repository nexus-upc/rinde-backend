package com.nexus.rinde.fleet.interfaces.rest.transform;

import com.nexus.rinde.fleet.domain.model.commands.RecordMaintenanceCommand;
import com.nexus.rinde.fleet.domain.model.entities.Maintenance;
import com.nexus.rinde.fleet.domain.model.valueobjects.MaintenanceAlert;
import com.nexus.rinde.fleet.interfaces.rest.resources.MaintenanceAlertResource;
import com.nexus.rinde.fleet.interfaces.rest.resources.MaintenanceResource;
import com.nexus.rinde.fleet.interfaces.rest.resources.RecordMaintenanceResource;
import java.util.UUID;

/** Transformadores de entidad Maintenance a recursos REST y viceversa. */
public final class MaintenanceResourceAssembler {

  private MaintenanceResourceAssembler() {}

  public static MaintenanceResource toResource(Maintenance entity) {
    return new MaintenanceResource(
        entity.getId(),
        entity.getVehicleId(),
        entity.getMaintenanceType(),
        entity.getExecutionDate(),
        entity.getMileage(),
        entity.getCost(),
        entity.getNextMaintenanceDate(),
        entity.getNotes(),
        entity.getCreatedAt());
  }

  public static MaintenanceAlertResource toAlertResource(MaintenanceAlert alert) {
    return new MaintenanceAlertResource(
        alert.vehicleId(),
        alert.plateNumber(),
        alert.state(),
        alert.nextMaintenanceDate(),
        alert.message());
  }

  public static RecordMaintenanceCommand toCommand(
      UUID tenantId, UUID vehicleId, RecordMaintenanceResource resource) {
    return new RecordMaintenanceCommand(
        tenantId,
        vehicleId != null ? vehicleId : resource.vehicleId(),
        resource.maintenanceType(),
        resource.executionDate(),
        resource.mileage(),
        resource.cost(),
        resource.nextMaintenanceDate(),
        resource.notes());
  }
}
