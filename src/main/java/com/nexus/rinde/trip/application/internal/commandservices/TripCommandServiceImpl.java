package com.nexus.rinde.trip.application.internal.commandservices;

import com.nexus.rinde.shared.domain.exceptions.ConflictException;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.domain.model.commands.AssignTripCommand;
import com.nexus.rinde.trip.domain.model.commands.FinishTripCommand;
import com.nexus.rinde.trip.domain.model.commands.ScheduleTripCommand;
import com.nexus.rinde.trip.domain.model.commands.StartTripCommand;
import com.nexus.rinde.trip.domain.model.valueobjects.Availability;
import com.nexus.rinde.trip.domain.model.valueobjects.Cargo;
import com.nexus.rinde.trip.domain.model.valueobjects.MaintenanceState;
import com.nexus.rinde.trip.domain.model.valueobjects.Route;
import com.nexus.rinde.trip.domain.services.FleetAvailabilityService;
import com.nexus.rinde.trip.domain.services.TripAssignmentResult;
import com.nexus.rinde.trip.domain.services.TripCommandService;
import com.nexus.rinde.trip.infrastructure.persistence.jpa.repositories.TripRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Coordina las reglas del agregado con la persistencia y las fachadas de otros contextos. */
@Service
public class TripCommandServiceImpl implements TripCommandService {

  private final TripRepository tripRepository;
  private final FleetAvailabilityService fleetAvailabilityService;
  private final Clock clock;

  public TripCommandServiceImpl(
      TripRepository tripRepository,
      FleetAvailabilityService fleetAvailabilityService,
      Clock clock) {
    this.tripRepository = tripRepository;
    this.fleetAvailabilityService = fleetAvailabilityService;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Trip handle(ScheduleTripCommand command) {
    Route route = new Route(command.origin(), command.destination());
    Cargo cargo = new Cargo(command.cargoDescription(), command.cargoWeightKg());
    Trip trip =
        Trip.schedule(
            command.tenantId(),
            tripRepository.nextCodeForTenant(command.tenantId()),
            route,
            cargo,
            command.departureDate(),
            command.confirmPastDate(),
            LocalDate.now(clock),
            clock.instant(),
            command.userId());
    return tripRepository.saveAndFlush(trip);
  }

  @Override
  @Transactional
  public TripAssignmentResult handle(AssignTripCommand command) {
    Trip trip = findTrip(command.tenantId(), command.tripId());
    trip.ensureAssignable();

    if (tripRepository.existsAssignmentOn(
        command.tenantId(),
        command.vehicleId(),
        trip.getDepartureDate(),
        true,
        trip.getId())) {
      throw new ConflictException("El vehículo ya está asignado a otro viaje en esa fecha.");
    }
    if (tripRepository.existsAssignmentOn(
        command.tenantId(),
        command.driverId(),
        trip.getDepartureDate(),
        false,
        trip.getId())) {
      throw new ConflictException("El conductor ya está asignado a otro viaje en esa fecha.");
    }

    Availability availability =
        fleetAvailabilityService.check(
            command.tenantId(), command.vehicleId(), command.driverId());
    trip.assign(
        command.vehicleId(),
        command.driverId(),
        availability,
        command.confirmOverdueMaintenance(),
        command.assignedBy(),
        clock.instant());
    tripRepository.saveAndFlush(trip);
    MaintenanceState alert =
        availability.maintenance() == MaintenanceState.DUE_SOON
            ? MaintenanceState.DUE_SOON
            : null;
    return new TripAssignmentResult(trip, alert);
  }

  @Override
  @Transactional
  public Trip handle(StartTripCommand command) {
    Trip trip = findTrip(command.tenantId(), command.tripId());
    fleetAvailabilityService
        .findDriverIdByUser(command.tenantId(), command.userId())
        .filter(trip::isAssignedTo)
        .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe."));
    trip.start(command.userId(), clock.instant());
    return tripRepository.saveAndFlush(trip);
  }

  @Override
  @Transactional
  public Trip handle(FinishTripCommand command) {
    Trip trip = findTrip(command.tenantId(), command.tripId());
    fleetAvailabilityService
        .findDriverIdByUser(command.tenantId(), command.userId())
        .filter(trip::isAssignedTo)
        .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe."));
    trip.finish(command.userId(), clock.instant());
    return tripRepository.saveAndFlush(trip);
  }

  private Trip findTrip(UUID tenantId, UUID tripId) {
    return tripRepository
        .findByIdAndTenantId(tripId, tenantId)
        .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe."));
  }
}
