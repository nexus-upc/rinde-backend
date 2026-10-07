package com.nexus.rinde.fleet.interfaces.acl;

import com.nexus.rinde.fleet.domain.model.aggregates.Driver;
import com.nexus.rinde.fleet.domain.model.queries.CheckDriverEligibilityQuery;
import com.nexus.rinde.fleet.domain.model.queries.GetVehicleHealthStatusQuery;
import com.nexus.rinde.fleet.domain.model.valueobjects.DriverEligibility;
import com.nexus.rinde.fleet.domain.model.valueobjects.VehicleHealthStatus;
import com.nexus.rinde.fleet.domain.services.DriverQueryService;
import com.nexus.rinde.fleet.domain.services.VehicleQueryService;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.DriverRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Implementación de la fachada de Fleet & Maintenance. */
@Component
public class FleetContextFacadeImpl implements FleetContextFacade {

  private final VehicleQueryService vehicleQueryService;
  private final DriverQueryService driverQueryService;
  private final DriverRepository driverRepository;

  public FleetContextFacadeImpl(
      VehicleQueryService vehicleQueryService,
      DriverQueryService driverQueryService,
      DriverRepository driverRepository) {
    this.vehicleQueryService = vehicleQueryService;
    this.driverQueryService = driverQueryService;
    this.driverRepository = driverRepository;
  }

  @Override
  public Optional<VehicleHealthStatus> findVehicleHealthStatus(UUID tenantId, UUID vehicleId) {
    try {
      return Optional.of(
          vehicleQueryService.handle(new GetVehicleHealthStatusQuery(tenantId, vehicleId)));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<DriverEligibility> checkDriverEligibility(UUID tenantId, UUID driverId) {
    try {
      return Optional.of(
          driverQueryService.handle(new CheckDriverEligibilityQuery(tenantId, driverId)));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<UUID> findDriverIdByUserId(UUID tenantId, UUID userId) {
    return driverRepository
        .findByTenantIdAndUserId(tenantId, userId)
        .map(Driver::getId);
  }
}
