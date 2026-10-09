package com.nexus.rinde.dashboard.application.internal.queryservices;

import com.nexus.rinde.dashboard.domain.model.queries.GetFleetStatusQuery;
import com.nexus.rinde.dashboard.domain.model.queries.GetOperationalMetricsQuery;
import com.nexus.rinde.dashboard.domain.model.queries.GetTripSummaryQuery;
import com.nexus.rinde.dashboard.domain.services.DashboardQueryService;
import com.nexus.rinde.dashboard.interfaces.rest.resources.FleetStatusResource;
import com.nexus.rinde.dashboard.interfaces.rest.resources.FleetStatusResource.DriverSummary;
import com.nexus.rinde.dashboard.interfaces.rest.resources.FleetStatusResource.VehicleSummary;
import com.nexus.rinde.dashboard.interfaces.rest.resources.OperationalMetricsResource;
import com.nexus.rinde.dashboard.interfaces.rest.resources.TripSummaryResource;
import com.nexus.rinde.expense.interfaces.acl.ExpenseContextFacade;
import com.nexus.rinde.expense.interfaces.acl.ExpenseSummary;
import com.nexus.rinde.fleet.domain.model.aggregates.Driver;
import com.nexus.rinde.fleet.domain.model.aggregates.Vehicle;
import com.nexus.rinde.fleet.domain.model.valueobjects.DriverStatus;
import com.nexus.rinde.fleet.domain.model.valueobjects.VehicleStatus;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.DriverRepository;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.nexus.rinde.settlement.domain.model.aggregates.Settlement;
import com.nexus.rinde.settlement.interfaces.acl.SettlementContextFacade;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.domain.model.valueobjects.TripStatus;
import com.nexus.rinde.trip.interfaces.acl.TripContextFacade;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación del servicio de consulta del dashboard operativo. */
@Service
public class DashboardQueryServiceImpl implements DashboardQueryService {

  private final TripContextFacade tripFacade;
  private final SettlementContextFacade settlementFacade;
  private final ExpenseContextFacade expenseFacade;
  private final VehicleRepository vehicleRepository;
  private final DriverRepository driverRepository;

  public DashboardQueryServiceImpl(
      TripContextFacade tripFacade,
      SettlementContextFacade settlementFacade,
      ExpenseContextFacade expenseFacade,
      VehicleRepository vehicleRepository,
      DriverRepository driverRepository) {
    this.tripFacade = tripFacade;
    this.settlementFacade = settlementFacade;
    this.expenseFacade = expenseFacade;
    this.vehicleRepository = vehicleRepository;
    this.driverRepository = driverRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public TripSummaryResource handle(GetTripSummaryQuery query) {
    Trip trip =
        tripFacade
            .findByIdAndTenantId(query.tripId(), query.tenantId())
            .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe."));

    List<ExpenseSummary> expenses =
        expenseFacade.findByTenantIdAndTripId(query.tenantId(), query.tripId());

    BigDecimal expenseTotal =
        expenses.stream()
            .filter(e -> "APPROVED".equals(e.status()))
            .map(e -> e.amount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    String expenseCurrency =
        expenses.stream()
            .filter(e -> "APPROVED".equals(e.status()))
            .map(e -> e.currency())
            .findFirst()
            .orElse("PEN");

    Settlement settlement =
        settlementFacade.findByTripIdAndTenantId(query.tripId(), query.tenantId()).orElse(null);

    UUID vehicleId = trip.getAssignment() != null ? trip.getAssignment().vehicleId() : null;
    UUID driverId = trip.getAssignment() != null ? trip.getAssignment().driverId() : null;

    return new TripSummaryResource(
        trip.getId(),
        trip.getCode(),
        trip.getStatus().name(),
        trip.getRoute().origin(),
        trip.getRoute().destination(),
        trip.getCargo().description(),
        trip.getCargo().weightKg(),
        trip.getDepartureDate(),
        vehicleId,
        driverId,
        trip.getStartedAt(),
        trip.getFinishedAt(),
        expenses.size(),
        expenseTotal,
        expenseCurrency,
        settlement != null ? settlement.getStatus().name() : null,
        settlement != null ? settlement.getAdvanceAmount() : null,
        settlement != null ? settlement.getAdvanceBalance() : null);
  }

  @Override
  @Transactional(readOnly = true)
  public FleetStatusResource handle(GetFleetStatusQuery query) {
    UUID tenantId = query.tenantId();

    List<Vehicle> vehicles = vehicleRepository.findByTenantId(tenantId);
    List<Driver> drivers = driverRepository.findByTenantId(tenantId);

    int available = (int) vehicles.stream().filter(v -> v.getStatus() == VehicleStatus.AVAILABLE).count();
    int inTrip = (int) vehicles.stream().filter(v -> v.getStatus() == VehicleStatus.IN_TRIP).count();
    int inMaintenance = (int) vehicles.stream().filter(v -> v.getStatus() == VehicleStatus.IN_MAINTENANCE).count();

    int enabled = (int) drivers.stream().filter(d -> d.getStatus() == DriverStatus.ENABLED).count();
    int disabled = (int) drivers.stream().filter(d -> d.getStatus() == DriverStatus.DISABLED).count();

    List<VehicleSummary> vehicleSummaries =
        vehicles.stream()
            .map(
                v ->
                    new VehicleSummary(
                        v.getId().toString(),
                        v.getPlateNumber(),
                        v.getBrand(),
                        v.getModel(),
                        v.getStatus().name()))
            .toList();

    List<DriverSummary> driverSummaries =
        drivers.stream()
            .map(
                d ->
                    new DriverSummary(
                        d.getId().toString(),
                        d.getFullName(),
                        d.getLicenseNumber(),
                        d.getLicenseCategory(),
                        d.getStatus().name()))
            .toList();

    return new FleetStatusResource(
        vehicles.size(),
        available,
        inTrip,
        inMaintenance,
        drivers.size(),
        enabled,
        disabled,
        vehicleSummaries,
        driverSummaries);
  }

  @Override
  @Transactional(readOnly = true)
  public OperationalMetricsResource handle(GetOperationalMetricsQuery query) {
    UUID tenantId = query.tenantId();

    List<Trip> trips = tripFacade.findByTenantId(tenantId);
    List<ExpenseSummary> expenses = expenseFacade.findByTenantId(tenantId);

    int scheduled = (int) trips.stream().filter(t -> t.getStatus() == TripStatus.SCHEDULED).count();
    int assigned = (int) trips.stream().filter(t -> t.getStatus() == TripStatus.ASSIGNED).count();
    int inRoute = (int) trips.stream().filter(t -> t.getStatus() == TripStatus.IN_ROUTE).count();
    int finished = (int) trips.stream().filter(t -> t.getStatus() == TripStatus.FINISHED).count();
    int settled = (int) trips.stream().filter(t -> t.getStatus() == TripStatus.SETTLED).count();

    int approved = (int) expenses.stream().filter(e -> "APPROVED".equals(e.status())).count();
    int pending =
        (int)
            expenses.stream()
                .filter(
                    e ->
                        "PENDING_SUPPORT".equals(e.status())
                            || "REGISTERED".equals(e.status()))
                .count();
    int observed = (int) expenses.stream().filter(e -> "OBSERVED".equals(e.status())).count();

    return new OperationalMetricsResource(
        trips.size(),
        scheduled,
        assigned,
        inRoute,
        finished,
        settled,
        expenses.size(),
        approved,
        pending,
        observed);
  }
}
