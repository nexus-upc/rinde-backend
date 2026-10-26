package com.nexus.rinde.trip.application.internal.queryservices;

import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.domain.model.queries.GetTripByIdQuery;
import com.nexus.rinde.trip.domain.model.queries.GetTripsAssignedToMeQuery;
import com.nexus.rinde.trip.domain.model.queries.GetTripsByTenantQuery;
import com.nexus.rinde.trip.domain.services.FleetAvailabilityService;
import com.nexus.rinde.trip.domain.services.TripQueryService;
import com.nexus.rinde.trip.infrastructure.persistence.jpa.repositories.TripRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta viajes solo dentro de la empresa y limita al conductor a sus propios viajes. */
@Service
public class TripQueryServiceImpl implements TripQueryService {

  private final TripRepository tripRepository;
  private final FleetAvailabilityService fleetAvailabilityService;

  public TripQueryServiceImpl(
      TripRepository tripRepository, FleetAvailabilityService fleetAvailabilityService) {
    this.tripRepository = tripRepository;
    this.fleetAvailabilityService = fleetAvailabilityService;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Trip> handle(GetTripsByTenantQuery query) {
    if (query.statuses() == null || query.statuses().isEmpty()) {
      return tripRepository.findByTenantId(query.tenantId());
    }
    return tripRepository.findByTenantIdAndStatusIn(query.tenantId(), query.statuses());
  }

  @Override
  @Transactional(readOnly = true)
  public Trip handle(GetTripByIdQuery query) {
    Trip trip =
        tripRepository
            .findByIdAndTenantId(query.tripId(), query.tenantId())
            .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe."));
    if ("DRIVER".equals(query.viewerRole())) {
      fleetAvailabilityService
          .findDriverIdByUser(query.tenantId(), query.viewerUserId())
          .filter(trip::isAssignedTo)
          .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe."));
    }
    return trip;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Trip> handle(GetTripsAssignedToMeQuery query) {
    return fleetAvailabilityService
        .findDriverIdByUser(query.tenantId(), query.userId())
        .map(driverId -> tripRepository.findByTenantAndDriver(query.tenantId(), driverId))
        .orElseGet(List::of);
  }
}
