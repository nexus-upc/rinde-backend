package com.nexus.rinde.trip.application.internal.outboundservices.acl;

import com.nexus.rinde.fleet.interfaces.acl.DriverEligibilitySummary;
import com.nexus.rinde.fleet.interfaces.acl.FleetContextFacade;
import com.nexus.rinde.fleet.interfaces.acl.VehicleHealthSummary;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import com.nexus.rinde.trip.domain.model.valueobjects.Availability;
import com.nexus.rinde.trip.domain.model.valueobjects.MaintenanceState;
import com.nexus.rinde.trip.domain.services.FleetAvailabilityService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Adaptador ACL de Trip hacia Fleet & Maintenance. Consulta la fachada de Fleet y traduce
 * las respuestas al modelo de disponibilidad que usa Trip para asignar viajes.
 */
@Service
public class FleetAvailabilityAclService implements FleetAvailabilityService {

  private final FleetContextFacade fleetContextFacade;

  public FleetAvailabilityAclService(FleetContextFacade fleetContextFacade) {
    this.fleetContextFacade = fleetContextFacade;
  }

  @Override
  public Availability check(UUID tenantId, UUID vehicleId, UUID driverId) {
    VehicleHealthSummary health =
        fleetContextFacade
            .findVehicleHealthStatus(tenantId, vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("El vehículo no existe."));
    DriverEligibilitySummary eligibility =
        fleetContextFacade
            .checkDriverEligibility(tenantId, driverId)
            .orElseThrow(() -> new ResourceNotFoundException("El conductor no existe."));

    MaintenanceState state = MaintenanceState.valueOf(health.maintenanceState());
    return new Availability(state, eligibility.eligible());
  }

  @Override
  public Optional<UUID> findDriverIdByUser(UUID tenantId, UUID userId) {
    return fleetContextFacade.findDriverIdByUserId(tenantId, userId);
  }
}
