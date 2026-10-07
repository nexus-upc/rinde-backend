package com.nexus.rinde.fleet.application.internal.commandservices;

import com.nexus.rinde.fleet.domain.model.aggregates.Vehicle;
import com.nexus.rinde.fleet.domain.model.commands.RegisterVehicleCommand;
import com.nexus.rinde.fleet.domain.services.VehicleCommandService;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.nexus.rinde.shared.domain.exceptions.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación del servicio de aplicación de comandos para Vehicle. */
@Service
@Transactional
public class VehicleCommandServiceImpl implements VehicleCommandService {

  private final VehicleRepository vehicleRepository;

  public VehicleCommandServiceImpl(VehicleRepository vehicleRepository) {
    this.vehicleRepository = vehicleRepository;
  }

  @Override
  public Vehicle handle(RegisterVehicleCommand command) {
    if (vehicleRepository.existsByTenantIdAndPlateNumber(
        command.tenantId(), command.plateNumber().trim().toUpperCase())) {
      throw new ConflictException("La placa ya se encuentra registrada en la empresa.");
    }

    Vehicle vehicle =
        new Vehicle(
            command.tenantId(),
            command.plateNumber(),
            command.brand(),
            command.model(),
            command.modelYear(),
            command.payloadCapacityKg());

    return vehicleRepository.save(vehicle);
  }
}
