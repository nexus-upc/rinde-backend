package com.nexus.rinde.fleet.application.internal.commandservices;

import com.nexus.rinde.fleet.domain.model.entities.Maintenance;
import com.nexus.rinde.fleet.domain.model.commands.RecordMaintenanceCommand;
import com.nexus.rinde.fleet.domain.services.MaintenanceCommandService;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.MaintenanceRepository;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación del servicio de aplicación de comandos para Maintenance. */
@Service
@Transactional
public class MaintenanceCommandServiceImpl implements MaintenanceCommandService {

  private final MaintenanceRepository maintenanceRepository;
  private final VehicleRepository vehicleRepository;

  public MaintenanceCommandServiceImpl(
      MaintenanceRepository maintenanceRepository, VehicleRepository vehicleRepository) {
    this.maintenanceRepository = maintenanceRepository;
    this.vehicleRepository = vehicleRepository;
  }

  @Override
  public Maintenance handle(RecordMaintenanceCommand command) {
    vehicleRepository
        .findByIdAndTenantId(command.vehicleId(), command.tenantId())
        .orElseThrow(() -> new ResourceNotFoundException("El vehículo no existe."));

    Maintenance maintenance =
        new Maintenance(
            command.tenantId(),
            command.vehicleId(),
            command.maintenanceType(),
            command.executionDate(),
            command.mileage(),
            command.cost(),
            command.nextMaintenanceDate(),
            command.notes());

    return maintenanceRepository.save(maintenance);
  }
}
