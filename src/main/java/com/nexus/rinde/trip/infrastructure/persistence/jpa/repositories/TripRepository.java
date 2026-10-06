package com.nexus.rinde.trip.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.domain.model.valueobjects.TripStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repositorio de viajes; cada consulta lleva el identificador de empresa. */
@Repository
public interface TripRepository extends JpaRepository<Trip, UUID>, TripRepositoryCustom {

  @EntityGraph(attributePaths = "statusChanges")
  Optional<Trip> findByIdAndTenantId(UUID id, UUID tenantId);

  List<Trip> findByTenantId(UUID tenantId);

  List<Trip> findByTenantIdAndStatusIn(UUID tenantId, Collection<TripStatus> statuses);

  @Query(
      "select case when count(trip) > 0 then true else false end from Trip trip "
          + "where trip.tenantId = :tenantId and trip.departureDate = :departureDate "
          + "and trip.status in (com.nexus.rinde.trip.domain.model.valueobjects.TripStatus.ASSIGNED, "
          + "com.nexus.rinde.trip.domain.model.valueobjects.TripStatus.IN_ROUTE) "
          + "and trip.id <> :excludedTripId "
          + "and (case when :vehicleResource = true then trip.assignment.vehicleId "
          + "else trip.assignment.driverId end) = :resourceId")
  boolean existsAssignmentOn(
      @Param("tenantId") UUID tenantId,
      @Param("resourceId") UUID resourceId,
      @Param("departureDate") LocalDate departureDate,
      @Param("vehicleResource") boolean vehicleResource,
      @Param("excludedTripId") UUID excludedTripId);

  @Query(
      "select trip from Trip trip where trip.tenantId = :tenantId "
          + "and trip.assignment.driverId = :driverId order by trip.departureDate asc")
  List<Trip> findByTenantAndDriver(
      @Param("tenantId") UUID tenantId, @Param("driverId") UUID driverId);
}
