package com.nexus.rinde.trip.interfaces.acl;

import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.infrastructure.persistence.jpa.repositories.TripRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Implementación de la fachada de Trip Management. */
@Component
public class TripContextFacadeImpl implements TripContextFacade {

  private final TripRepository tripRepository;

  public TripContextFacadeImpl(TripRepository tripRepository) {
    this.tripRepository = tripRepository;
  }

  @Override
  public Optional<Trip> findByIdAndTenantId(UUID tripId, UUID tenantId) {
    return tripRepository.findByIdAndTenantId(tripId, tenantId);
  }

  @Override
  public List<Trip> findByTenantId(UUID tenantId) {
    return tripRepository.findByTenantId(tenantId);
  }
}
