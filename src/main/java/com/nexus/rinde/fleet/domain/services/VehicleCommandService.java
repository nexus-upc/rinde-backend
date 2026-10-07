package com.nexus.rinde.fleet.domain.services;

import com.nexus.rinde.fleet.domain.model.aggregates.Vehicle;
import com.nexus.rinde.fleet.domain.model.commands.RegisterVehicleCommand;

/** Servicio de aplicación para comandos sobre el agregado Vehicle. */
public interface VehicleCommandService {

  Vehicle handle(RegisterVehicleCommand command);
}
