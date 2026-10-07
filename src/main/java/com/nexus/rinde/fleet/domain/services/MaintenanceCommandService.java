package com.nexus.rinde.fleet.domain.services;

import com.nexus.rinde.fleet.domain.model.commands.RecordMaintenanceCommand;
import com.nexus.rinde.fleet.domain.model.entities.Maintenance;

/** Servicio de aplicación para comandos sobre el mantenimiento de vehículos. */
public interface MaintenanceCommandService {

  Maintenance handle(RecordMaintenanceCommand command);
}
