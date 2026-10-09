package com.nexus.rinde.trip.interfaces.acl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Fachada pública del Bounded Context Trip Management para otros contextos. */
public interface TripContextFacade {

  Optional<TripSummary> findByIdAndTenantId(UUID tripId, UUID tenantId);

  List<TripSummary> findByTenantId(UUID tenantId);
}
