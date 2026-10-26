package com.nexus.rinde.fleet.application.internal.commandservices;

import com.nexus.rinde.fleet.domain.model.aggregates.Vehicle;
import com.nexus.rinde.fleet.domain.model.entities.Maintenance;
import com.nexus.rinde.fleet.domain.model.valueobjects.VehicleHealthStatus;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.MaintenanceRepository;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.nexus.rinde.fleet.interfaces.acl.MaintenanceDue;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Anuncia los mantenimientos próximos o vencidos de todas las empresas. Solo avisa cuando el
 * estado de la unidad cambia respecto del último anunciado.
 */
@Service
public class MaintenanceDueAnnouncementService {

  private final VehicleRepository vehicleRepository;
  private final MaintenanceRepository maintenanceRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final Clock clock;

  public MaintenanceDueAnnouncementService(
      VehicleRepository vehicleRepository,
      MaintenanceRepository maintenanceRepository,
      ApplicationEventPublisher eventPublisher,
      Clock clock) {
    this.vehicleRepository = vehicleRepository;
    this.maintenanceRepository = maintenanceRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  /** Devuelve la cantidad de anuncios publicados en esta revisión. */
  @Transactional
  public int announceDueMaintenances() {
    LocalDate today = LocalDate.now(clock);
    Instant now = clock.instant();
    int announced = 0;
    for (Vehicle vehicle : vehicleRepository.findAll()) {
      List<Maintenance> maintenances =
          maintenanceRepository.findByTenantIdAndVehicleId(vehicle.getTenantId(), vehicle.getId());
      VehicleHealthStatus health = vehicle.evaluateHealth(today, maintenances);
      boolean mustAnnounce = vehicle.announceMaintenanceState(health.maintenanceState());
      vehicleRepository.save(vehicle);
      if (mustAnnounce) {
        eventPublisher.publishEvent(
            new MaintenanceDue(
                UUID.randomUUID(),
                now,
                vehicle.getTenantId(),
                vehicle.getId(),
                vehicle.getPlateNumber(),
                health.maintenanceState().name(),
                health.nextMaintenanceDate()));
        announced++;
      }
    }
    return announced;
  }
}
