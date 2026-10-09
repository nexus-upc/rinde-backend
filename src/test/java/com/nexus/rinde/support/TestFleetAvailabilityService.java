package com.nexus.rinde.support;

import com.nexus.rinde.iam.interfaces.acl.IamContextFacade;
import com.nexus.rinde.trip.application.internal.outboundservices.acl.FleetAvailabilityAclService;
import com.nexus.rinde.trip.domain.model.valueobjects.Availability;
import com.nexus.rinde.trip.domain.model.valueobjects.MaintenanceState;
import com.nexus.rinde.trip.domain.services.FleetAvailabilityService;
import java.util.Optional;
import java.util.UUID;

/** Doble configurable para probar respuestas de Fleet & Maintenance en cada escenario. */
public class TestFleetAvailabilityService implements FleetAvailabilityService {

  private final IamContextFacade iamContextFacade;
  private final FleetAvailabilityAclService realAdapter;
  private volatile MaintenanceState maintenanceState = MaintenanceState.UP_TO_DATE;
  private volatile boolean driverEnabled = true;
  private volatile boolean useRealAdapter = false;

  public TestFleetAvailabilityService(
      IamContextFacade iamContextFacade, FleetAvailabilityAclService realAdapter) {
    this.iamContextFacade = iamContextFacade;
    this.realAdapter = realAdapter;
  }

  @Override
  public Availability check(UUID tenantId, UUID vehicleId, UUID driverId) {
    if (useRealAdapter) {
      return realAdapter.check(tenantId, vehicleId, driverId);
    }
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
    useRealAdapter = false;
  }

  /** Si es verdadero, la consulta de disponibilidad usa el adaptador real contra Fleet. */
  public void useRealAdapter(boolean enabled) {
    useRealAdapter = enabled;
  }

  public void setMaintenanceState(MaintenanceState state) {
    maintenanceState = state;
  }

  public void setDriverEnabled(boolean enabled) {
    driverEnabled = enabled;
  }
}
