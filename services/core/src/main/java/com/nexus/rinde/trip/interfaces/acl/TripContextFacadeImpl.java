package com.nexus.rinde.trip.interfaces.acl;

import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.domain.model.valueobjects.Assignment;
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
  public Optional<TripSummary> findByIdAndTenantId(UUID tripId, UUID tenantId) {
    return tripRepository.findByIdAndTenantId(tripId, tenantId).map(TripContextFacadeImpl::toSummary);
  }

  @Override
  public List<TripSummary> findByTenantId(UUID tenantId) {
    return tripRepository.findByTenantId(tenantId).stream()
        .map(TripContextFacadeImpl::toSummary)
        .toList();
  }

  private static TripSummary toSummary(Trip trip) {
    Assignment assignment = trip.getAssignment();
    return new TripSummary(
        trip.getId(),
        trip.getCode(),
        trip.getStatus().name(),
        trip.getRoute().origin(),
        trip.getRoute().destination(),
        trip.getCargo().description(),
        trip.getCargo().weightKg(),
        trip.getDepartureDate(),
        assignment != null ? assignment.vehicleId() : null,
        assignment != null ? assignment.driverId() : null,
        trip.getStartedAt(),
        trip.getFinishedAt());
  }
}
