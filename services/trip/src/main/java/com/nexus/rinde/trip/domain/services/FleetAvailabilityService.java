package com.nexus.rinde.trip.domain.services;

import com.nexus.rinde.trip.domain.model.valueobjects.Availability;
import java.util.Optional;
import java.util.UUID;

/** Puerto de Trip hacia Fleet & Maintenance; su implementación es el Anticorruption Layer. */
public interface FleetAvailabilityService {

  Availability check(UUID tenantId, UUID vehicleId, UUID driverId);

  Optional<UUID> findDriverIdByUser(UUID tenantId, UUID userId);
}
