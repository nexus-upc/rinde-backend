package com.nexus.rinde.fleet.interfaces.acl;

import java.util.Optional;
import java.util.UUID;

/**
 * Fachada pública del Bounded Context Fleet & Maintenance para otros contextos.
 * Permite consultar el estado de salud de unidades y la elegibilidad de conductores.
 */
public interface FleetContextFacade {

  Optional<VehicleHealthSummary> findVehicleHealthStatus(UUID tenantId, UUID vehicleId);

  Optional<DriverEligibilitySummary> checkDriverEligibility(UUID tenantId, UUID driverId);

  Optional<UUID> findDriverIdByUserId(UUID tenantId, UUID userId);

  /** Número de unidades registradas en la empresa indicada. */
  long countVehiclesByTenantId(UUID tenantId);
}
