package com.nexus.rinde.fleet.interfaces.internal;

import com.nexus.rinde.fleet.interfaces.acl.*;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

/** Contratos HTTP de Fleet durante la transición al servicio independiente. */
@RestController
@RequestMapping("/api/internal/v1/fleet/tenants/{tenantId}")
public class InternalFleetController {
  private final FleetContextFacade fleet;

  public InternalFleetController(FleetContextFacade fleet) {
    this.fleet = fleet;
  }

  @GetMapping("/vehicles/{vehicleId}/health")
  public VehicleHealthSummary health(@PathVariable UUID tenantId, @PathVariable UUID vehicleId) {
    return fleet
        .findVehicleHealthStatus(tenantId, vehicleId)
        .orElseThrow(() -> new ResourceNotFoundException("El vehículo no existe."));
  }

  @GetMapping("/drivers/{driverId}/eligibility")
  public DriverEligibilitySummary eligibility(
      @PathVariable UUID tenantId, @PathVariable UUID driverId) {
    return fleet
        .checkDriverEligibility(tenantId, driverId)
        .orElseThrow(() -> new ResourceNotFoundException("El conductor no existe."));
  }

  @GetMapping("/users/{userId}/driver")
  public DriverId driver(@PathVariable UUID tenantId, @PathVariable UUID userId) {
    return fleet
        .findDriverIdByUserId(tenantId, userId)
        .map(DriverId::new)
        .orElseThrow(
            () -> new ResourceNotFoundException("El usuario no tiene conductor registrado."));
  }

  public record DriverId(UUID driverId) {}
}
