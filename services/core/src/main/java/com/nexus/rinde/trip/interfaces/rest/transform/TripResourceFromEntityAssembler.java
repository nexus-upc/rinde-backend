package com.nexus.rinde.trip.interfaces.rest.transform;

import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.domain.model.entities.StatusChange;
import com.nexus.rinde.trip.domain.model.valueobjects.Assignment;
import com.nexus.rinde.trip.domain.services.TripAssignmentResult;
import com.nexus.rinde.trip.interfaces.rest.resources.TripAssignmentResponseResource;
import com.nexus.rinde.trip.interfaces.rest.resources.TripDetailResource;
import com.nexus.rinde.trip.interfaces.rest.resources.TripStatusChangeResource;
import com.nexus.rinde.trip.interfaces.rest.resources.TripSummaryResource;
import java.util.List;

/** Convierte viajes y cambios de estado en los recursos de la API. */
public final class TripResourceFromEntityAssembler {

  private TripResourceFromEntityAssembler() {}

  public static TripSummaryResource toSummaryResource(Trip trip) {
    Assignment assignment = trip.getAssignment();
    return new TripSummaryResource(
        trip.getId(),
        trip.getCode(),
        trip.getRoute().origin(),
        trip.getRoute().destination(),
        trip.getDepartureDate(),
        assignment == null ? null : assignment.vehicleId(),
        assignment == null ? null : assignment.driverId(),
        trip.getStatus().name());
  }

  public static TripDetailResource toDetailResource(Trip trip) {
    Assignment assignment = trip.getAssignment();
    List<TripStatusChangeResource> changes =
        trip.getStatusChanges().stream().map(TripResourceFromEntityAssembler::toResource).toList();
    return new TripDetailResource(
        trip.getId(),
        trip.getCode(),
        trip.getRoute().origin(),
        trip.getRoute().destination(),
        trip.getCargo().description(),
        trip.getCargo().weightKg(),
        trip.getDepartureDate(),
        trip.getStatus().name(),
        assignment == null ? null : assignment.vehicleId(),
        assignment == null ? null : assignment.driverId(),
        assignment == null ? null : assignment.assignedAt(),
        assignment == null ? null : assignment.overdueMaintenanceConfirmedBy(),
        trip.getStartedAt(),
        trip.getFinishedAt(),
        changes);
  }

  public static TripAssignmentResponseResource toAssignmentResource(
      TripAssignmentResult result) {
    Assignment assignment = result.trip().getAssignment();
    return new TripAssignmentResponseResource(
        result.trip().getId(),
        result.trip().getCode(),
        result.trip().getStatus().name(),
        assignment.vehicleId(),
        assignment.driverId(),
        assignment.assignedAt(),
        result.maintenanceAlert() == null ? null : result.maintenanceAlert().name());
  }

  private static TripStatusChangeResource toResource(StatusChange change) {
    return new TripStatusChangeResource(
        change.getStatus().name(), change.getChangedAt(), change.getChangedBy());
  }
}
