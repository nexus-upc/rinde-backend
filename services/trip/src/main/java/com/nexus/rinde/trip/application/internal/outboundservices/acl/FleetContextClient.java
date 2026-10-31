package com.nexus.rinde.trip.application.internal.outboundservices.acl;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto hacia las consultas internas de Fleet & Maintenance. Devuelve vacío cuando Fleet responde
 * que el recurso no existe en la empresa y lanza FleetUnavailableException si no puede responder.
 */
public interface FleetContextClient {

  Optional<FleetVehicleHealth> findVehicleHealth(UUID tenantId, UUID vehicleId);

  Optional<FleetDriverEligibility> checkDriverEligibility(UUID tenantId, UUID driverId);

  Optional<UUID> findDriverIdByUserId(UUID tenantId, UUID userId);
}
