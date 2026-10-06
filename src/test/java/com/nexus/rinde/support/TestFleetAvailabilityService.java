package com.nexus.rinde.support;

import com.nexus.rinde.iam.interfaces.acl.IamContextFacade;
import com.nexus.rinde.trip.domain.model.valueobjects.Availability;
import com.nexus.rinde.trip.domain.model.valueobjects.MaintenanceState;
import com.nexus.rinde.trip.domain.services.FleetAvailabilityService;
import java.util.Optional;
import java.util.UUID;

/** Doble configurable para probar respuestas de Fleet & Maintenance en cada escenario. */
public class TestFleetAvailabilityService implements FleetAvailabilityService {

  private final IamContextFacade iamContextFacade;
  private volatile MaintenanceState maintenanceState = MaintenanceState.UP_TO_DATE;
  private volatile boolean driverEnabled = true;

  public TestFleetAvailabilityService(IamContextFacade iamContextFacade) {
    this.iamContextFacade = iamContextFacade;
  }

  @Override
  public Availability check(UUID tenantId, UUID vehicleId, UUID driverId) {
    return new Availability(maintenanceState, driverEnabled);
  }

  @Override
  public Optional<UUID> findDriverIdByUser(UUID tenantId, UUID userId) {
    return iamContextFacade.isActiveUserWithRole(tenantId, userId, "DRIVER")
        ? Optional.of(userId)
        : Optional.empty();
  }

  public void reset() {
    maintenanceState = MaintenanceState.UP_TO_DATE;
    driverEnabled = true;
  }

  public void setMaintenanceState(MaintenanceState state) {
    maintenanceState = state;
  }

  public void setDriverEnabled(boolean enabled) {
    driverEnabled = enabled;
  }
}
