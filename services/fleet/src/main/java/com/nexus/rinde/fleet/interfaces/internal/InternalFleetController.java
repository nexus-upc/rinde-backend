package com.nexus.rinde.fleet.interfaces.internal;

import com.nexus.rinde.fleet.interfaces.acl.*;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

/** Contratos HTTP de Fleet durante la transición al servicio independiente. */
@RestController
@RequestMapping("/api/internal/v1/fleet/tenants/{tenantId}")
@Tag(name = "Internal", description = "Consultas entre servicios de RINDE (no públicas)")
public class InternalFleetController {
  private final FleetContextFacade fleet;

  public InternalFleetController(FleetContextFacade fleet) {
    this.fleet = fleet;
  }

  @Operation(summary = "Estado de mantenimiento de una unidad")
  @GetMapping("/vehicles/{vehicleId}/health")
  public VehicleHealthSummary health(@PathVariable UUID tenantId, @PathVariable UUID vehicleId) {
    return fleet
        .findVehicleHealthStatus(tenantId, vehicleId)
        .orElseThrow(() -> new ResourceNotFoundException("El vehículo no existe."));
  }

  @Operation(summary = "Habilitación de un conductor")
  @GetMapping("/drivers/{driverId}/eligibility")
  public DriverEligibilitySummary eligibility(
      @PathVariable UUID tenantId, @PathVariable UUID driverId) {
    return fleet
        .checkDriverEligibility(tenantId, driverId)
        .orElseThrow(() -> new ResourceNotFoundException("El conductor no existe."));
  }

  @Operation(summary = "Conductor asociado a un usuario")
  @GetMapping("/users/{userId}/driver")
  public DriverId driver(@PathVariable UUID tenantId, @PathVariable UUID userId) {
    return fleet
        .findDriverIdByUserId(tenantId, userId)
        .map(DriverId::new)
        .orElseThrow(
            () -> new ResourceNotFoundException("El usuario no tiene conductor registrado."));
  }

  @Operation(summary = "Cantidad de unidades registradas por empresa")
  @GetMapping("/vehicles/count")
  public VehicleCount count(@PathVariable UUID tenantId) {
    return new VehicleCount(fleet.countVehiclesByTenantId(tenantId));
  }

  @Operation(summary = "Unidades registradas por empresa")
  @GetMapping("/vehicles")
  public List<FleetVehicleSummary> vehicles(@PathVariable UUID tenantId) {
    return fleet.findVehiclesByTenantId(tenantId);
  }

  @Operation(summary = "Conductores registrados por empresa")
  @GetMapping("/drivers")
  public List<FleetDriverSummary> drivers(@PathVariable UUID tenantId) {
    return fleet.findDriversByTenantId(tenantId);
  }

  public record DriverId(UUID driverId) {}

  public record VehicleCount(long count) {}
}
