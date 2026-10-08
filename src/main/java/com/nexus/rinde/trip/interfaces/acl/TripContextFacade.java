package com.nexus.rinde.trip.interfaces.acl;

import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Fachada pública del Bounded Context Trip Management para otros contextos. */
public interface TripContextFacade {

  Optional<Trip> findByIdAndTenantId(UUID tripId, UUID tenantId);

  List<Trip> findByTenantId(UUID tenantId);
}
