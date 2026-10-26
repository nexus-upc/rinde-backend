package com.nexus.rinde.trip.application.internal.queryservices;

import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.domain.services.TripInternalQueryService;
import com.nexus.rinde.trip.infrastructure.persistence.jpa.repositories.TripRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de las consultas internas de viajes. */
@Service
public class TripInternalQueryServiceImpl implements TripInternalQueryService {

  private final TripRepository tripRepository;

  public TripInternalQueryServiceImpl(TripRepository tripRepository) {
    this.tripRepository = tripRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Trip> findByTenant(UUID tenantId) {
    return tripRepository.findByTenantId(tenantId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Trip> findByTripAndTenant(UUID tenantId, UUID tripId) {
    return tripRepository.findByIdAndTenantId(tripId, tenantId);
  }
}
