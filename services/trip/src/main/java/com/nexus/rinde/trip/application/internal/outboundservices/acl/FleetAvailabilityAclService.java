package com.nexus.rinde.trip.application.internal.outboundservices.acl;

import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import com.nexus.rinde.trip.domain.model.valueobjects.Availability;
import com.nexus.rinde.trip.domain.model.valueobjects.MaintenanceState;
import com.nexus.rinde.trip.domain.services.FleetAvailabilityService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Adaptador ACL de Trip hacia Fleet & Maintenance. Consulta Fleet por HTTP y traduce las respuestas
 * al modelo de disponibilidad que usa Trip para asignar viajes.
 */
@Service
public class FleetAvailabilityAclService implements FleetAvailabilityService {

  private final FleetContextClient fleetContextClient;

  public FleetAvailabilityAclService(FleetContextClient fleetContextClient) {
    this.fleetContextClient = fleetContextClient;
  }

  @Override
  public Availability check(UUID tenantId, UUID vehicleId, UUID driverId) {
    FleetVehicleHealth health =
        fleetContextClient
            .findVehicleHealth(tenantId, vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("El vehículo no existe."));
    FleetDriverEligibility eligibility =
        fleetContextClient
            .checkDriverEligibility(tenantId, driverId)
            .orElseThrow(() -> new ResourceNotFoundException("El conductor no existe."));
    return new Availability(toMaintenanceState(health.maintenanceState()), eligibility.eligible());
  }

  @Override
  public Optional<UUID> findDriverIdByUser(UUID tenantId, UUID userId) {
    return fleetContextClient.findDriverIdByUserId(tenantId, userId);
  }

  /** Un estado que Trip no reconoce indica un contrato roto con Fleet, no un error del cliente. */
  private static MaintenanceState toMaintenanceState(String value) {
    try {
      return MaintenanceState.valueOf(value);
    } catch (IllegalArgumentException | NullPointerException ex) {
      throw new FleetUnavailableException();
    }
  }
}
