package com.nexus.rinde.trip.application.internal.outboundservices.acl;

import com.nexus.rinde.fleet.interfaces.acl.DriverEligibilitySummary;
import com.nexus.rinde.fleet.interfaces.acl.FleetContextFacade;
import com.nexus.rinde.fleet.interfaces.acl.VehicleHealthSummary;
import com.nexus.rinde.iam.interfaces.acl.IamContextFacade;
import com.nexus.rinde.trip.domain.model.valueobjects.Availability;
import com.nexus.rinde.trip.domain.model.valueobjects.MaintenanceState;
import com.nexus.rinde.trip.domain.services.FleetAvailabilityService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Adaptador ACL de Trip hacia Fleet & Maintenance. Consulta la fachada de Fleet y traduce
 * las respuestas técnicas al modelo de disponibilidad requerido para asignar viajes.
 */
@Service
public class ProvisionalFleetAvailabilityService implements FleetAvailabilityService {

  private final IamContextFacade iamContextFacade;
  private final FleetContextFacade fleetContextFacade;

  public ProvisionalFleetAvailabilityService(
      IamContextFacade iamContextFacade, FleetContextFacade fleetContextFacade) {
    this.iamContextFacade = iamContextFacade;
    this.fleetContextFacade = fleetContextFacade;
  }

  @Override
  public Availability check(UUID tenantId, UUID vehicleId, UUID driverId) {
    Optional<VehicleHealthSummary> health =
        fleetContextFacade.findVehicleHealthStatus(tenantId, vehicleId);
    Optional<DriverEligibilitySummary> eligibility =
        fleetContextFacade.checkDriverEligibility(tenantId, driverId);

    if (health.isPresent() && eligibility.isPresent()) {
      MaintenanceState state =
          switch (health.get().maintenanceState()) {
            case "OVERDUE" -> MaintenanceState.OVERDUE;
            case "DUE_SOON" -> MaintenanceState.DUE_SOON;
            default -> MaintenanceState.UP_TO_DATE;
          };
      return new Availability(state, eligibility.get().eligible());
    }

    return new Availability(MaintenanceState.UP_TO_DATE, true);
  }

  @Override
  public Optional<UUID> findDriverIdByUser(UUID tenantId, UUID userId) {
    Optional<UUID> driverId = fleetContextFacade.findDriverIdByUserId(tenantId, userId);
    if (driverId.isPresent()) {
      return driverId;
    }
    return iamContextFacade.isActiveUserWithRole(tenantId, userId, "DRIVER")
        ? Optional.of(userId)
        : Optional.empty();
  }
}
