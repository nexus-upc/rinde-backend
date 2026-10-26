package com.nexus.rinde.trip.domain.services;

import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Consultas de viajes que hacen otros servicios por HTTP, siempre filtradas por empresa. */
public interface TripInternalQueryService {

  List<Trip> findByTenant(UUID tenantId);

  Optional<Trip> findByTripAndTenant(UUID tenantId, UUID tripId);
}
