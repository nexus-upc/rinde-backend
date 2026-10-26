package com.nexus.rinde.trip.interfaces.internal.transform;

import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.domain.model.valueobjects.Assignment;
import com.nexus.rinde.trip.interfaces.internal.resources.InternalTripResource;
import com.nexus.rinde.trip.interfaces.internal.resources.TripStatusResource;

/** Convierte el agregado Trip en los recursos que se exponen a otros servicios. */
public final class InternalTripResourceAssembler {

  private InternalTripResourceAssembler() {}

  public static InternalTripResource toResource(Trip trip) {
    Assignment assignment = trip.getAssignment();
    return new InternalTripResource(
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

  public static TripStatusResource toStatusResource(Trip trip) {
    return new TripStatusResource(trip.getId(), trip.getStatus().name());
  }
}
