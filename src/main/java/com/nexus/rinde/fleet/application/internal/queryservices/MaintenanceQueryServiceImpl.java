package com.nexus.rinde.fleet.application.internal.queryservices;

import com.nexus.rinde.fleet.domain.model.aggregates.Vehicle;
import com.nexus.rinde.fleet.domain.model.entities.Maintenance;
import com.nexus.rinde.fleet.domain.model.queries.ListMaintenanceAlertsQuery;
import com.nexus.rinde.fleet.domain.model.valueobjects.MaintenanceAlert;
import com.nexus.rinde.fleet.domain.model.valueobjects.MaintenanceState;
import com.nexus.rinde.fleet.domain.model.valueobjects.VehicleHealthStatus;
import com.nexus.rinde.fleet.domain.services.MaintenanceQueryService;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.MaintenanceRepository;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.VehicleRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación del servicio de aplicación de consultas para Maintenance. */
@Service
@Transactional(readOnly = true)
public class MaintenanceQueryServiceImpl implements MaintenanceQueryService {

  private final VehicleRepository vehicleRepository;
  private final MaintenanceRepository maintenanceRepository;

  public MaintenanceQueryServiceImpl(
      VehicleRepository vehicleRepository, MaintenanceRepository maintenanceRepository) {
    this.vehicleRepository = vehicleRepository;
    this.maintenanceRepository = maintenanceRepository;
  }

  @Override
  public List<MaintenanceAlert> handle(ListMaintenanceAlertsQuery query) {
    List<Vehicle> vehicles = vehicleRepository.findByTenantId(query.tenantId());
    List<MaintenanceAlert> alerts = new ArrayList<>();
    LocalDate today = LocalDate.now();

    for (Vehicle vehicle : vehicles) {
      List<Maintenance> maintenances =
          maintenanceRepository.findByTenantIdAndVehicleId(query.tenantId(), vehicle.getId());
      VehicleHealthStatus health = vehicle.evaluateHealth(today, maintenances);

      if (health.maintenanceState() == MaintenanceState.DUE_SOON
          || health.maintenanceState() == MaintenanceState.OVERDUE) {
        String msg =
            health.maintenanceState() == MaintenanceState.OVERDUE
                ? "La unidad tiene el mantenimiento vencido."
                : "La unidad tiene un mantenimiento próximo.";
        alerts.add(
            new MaintenanceAlert(
                vehicle.getId(),
                vehicle.getPlateNumber(),
                health.maintenanceState(),
                health.nextMaintenanceDate(),
                msg));
      }
    }
    return alerts;
  }
}
