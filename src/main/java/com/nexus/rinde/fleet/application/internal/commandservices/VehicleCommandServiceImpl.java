package com.nexus.rinde.fleet.application.internal.commandservices;

import com.nexus.rinde.fleet.domain.model.aggregates.Vehicle;
import com.nexus.rinde.fleet.domain.model.commands.RegisterVehicleCommand;
import com.nexus.rinde.fleet.domain.services.VehicleCommandService;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.TenantPlanLimitRepository;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.nexus.rinde.shared.domain.exceptions.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación del servicio de aplicación de comandos para Vehicle. */
@Service
@Transactional
public class VehicleCommandServiceImpl implements VehicleCommandService {

  private final VehicleRepository vehicleRepository;
  private final TenantPlanLimitRepository planLimitRepository;

  public VehicleCommandServiceImpl(
      VehicleRepository vehicleRepository,
      TenantPlanLimitRepository planLimitRepository) {
    this.vehicleRepository = vehicleRepository;
    this.planLimitRepository = planLimitRepository;
  }

  @Override
  public Vehicle handle(RegisterVehicleCommand command) {
    if (vehicleRepository.existsByTenantIdAndPlateNumber(
        command.tenantId(), command.plateNumber().trim().toUpperCase())) {
      throw new ConflictException("La placa ya se encuentra registrada en la empresa.");
    }

    planLimitRepository
        .findById(command.tenantId())
        .ifPresent(
            planLimit -> {
              long registeredUnits = vehicleRepository.countByTenantId(command.tenantId());
              if (registeredUnits >= planLimit.getUnitLimit()) {
                throw new ConflictException(
                    "El plan alcanzó su límite de "
                        + planLimit.getUnitLimit()
                        + " unidades. Seleccione un plan con mayor capacidad desde /api/v1/plans.");
              }
            });

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
