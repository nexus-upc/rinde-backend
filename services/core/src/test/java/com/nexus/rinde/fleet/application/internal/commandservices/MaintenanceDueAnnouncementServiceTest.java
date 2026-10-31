package com.nexus.rinde.fleet.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexus.rinde.fleet.domain.model.aggregates.Vehicle;
import com.nexus.rinde.fleet.domain.model.entities.Maintenance;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.MaintenanceRepository;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.nexus.rinde.fleet.interfaces.acl.MaintenanceDue;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

/** Prueba los anuncios de mantenimiento con un reloj controlable y repositorios simulados. */
class MaintenanceDueAnnouncementServiceTest {

  private static final LocalDate OCT_8 = LocalDate.of(2026, 10, 8);

  private final UUID tenantId = UUID.randomUUID();
  private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
  private final MaintenanceRepository maintenanceRepository = mock(MaintenanceRepository.class);
  private final ApplicationEventPublisher eventPublisher =
      mock(ApplicationEventPublisher.class);
  private final MutableClock clock = new MutableClock();
  private final Vehicle vehicle =
      new Vehicle(tenantId, "ABC-123", "Volvo", "FH", 2022, new BigDecimal("5000"));
  private MaintenanceDueAnnouncementService service;

  @BeforeEach
  void setUp() {
    when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));
    service =
        new MaintenanceDueAnnouncementService(
            vehicleRepository, maintenanceRepository, eventPublisher, clock);
  }

  @Test
  void publishesOnceWhenMaintenanceBecomesDueSoon() {
    nextMaintenanceOn(OCT_8.plusDays(5));
    runOn(OCT_8);
    runOn(OCT_8.plusDays(1));

    ArgumentCaptor<MaintenanceDue> sent = ArgumentCaptor.forClass(MaintenanceDue.class);
    verify(eventPublisher, times(1)).publishEvent(sent.capture());
    MaintenanceDue event = sent.getValue();
    assertThat(event.maintenanceState()).isEqualTo("DUE_SOON");
    assertThat(event.plateNumber()).isEqualTo("ABC-123");
    assertThat(event.vehicleId()).isEqualTo(vehicle.getId());
    assertThat(event.tenantId()).isEqualTo(tenantId);
    assertThat(event.nextMaintenanceDate()).isEqualTo(OCT_8.plusDays(5));
  }

  @Test
  void publishesAgainWhenMaintenanceBecomesOverdue() {
    nextMaintenanceOn(OCT_8.plusDays(5));
    runOn(OCT_8);
    runOn(OCT_8.plusDays(6));

    ArgumentCaptor<MaintenanceDue> sent = ArgumentCaptor.forClass(MaintenanceDue.class);
    verify(eventPublisher, times(2)).publishEvent(sent.capture());
    assertThat(sent.getAllValues().get(0).maintenanceState()).isEqualTo("DUE_SOON");
    assertThat(sent.getAllValues().get(1).maintenanceState()).isEqualTo("OVERDUE");
  }

  @Test
  void announcesAgainOnlyAfterVehicleWentBackToUpToDate() {
    nextMaintenanceOn(OCT_8.plusDays(5));
    runOn(OCT_8);

    nextMaintenanceOn(LocalDate.of(2026, 12, 31));
    runOn(OCT_8.plusDays(1));
    verify(eventPublisher, times(1)).publishEvent(any(MaintenanceDue.class));

    nextMaintenanceOn(LocalDate.of(2027, 1, 5));
    runOn(LocalDate.of(2026, 12, 30));

    ArgumentCaptor<MaintenanceDue> sent = ArgumentCaptor.forClass(MaintenanceDue.class);
    verify(eventPublisher, times(2)).publishEvent(sent.capture());
    assertThat(sent.getAllValues().get(1).maintenanceState()).isEqualTo("DUE_SOON");
  }

  @Test
  void publishesNothingForUpToDateVehicles() {
    nextMaintenanceOn(LocalDate.of(2026, 12, 31));
    runOn(OCT_8);
    runOn(OCT_8.plusDays(1));

    verify(eventPublisher, never()).publishEvent(any(MaintenanceDue.class));
  }

  private void nextMaintenanceOn(LocalDate nextMaintenanceDate) {
    Maintenance maintenance =
        new Maintenance(
            tenantId,
            vehicle.getId(),
            "PREVENTIVO",
            nextMaintenanceDate.minusMonths(1),
            25000,
            new BigDecimal("300.00"),
            nextMaintenanceDate,
            "Servicio programado");
    when(maintenanceRepository.findByTenantIdAndVehicleId(tenantId, vehicle.getId()))
        .thenReturn(List.of(maintenance));
  }

  private void runOn(LocalDate day) {
    clock.moveTo(day);
    service.announceDueMaintenances();
  }

  /** Reloj que se mueve a mano para simular cada ejecución diaria a las 06:00 UTC. */
  private static final class MutableClock extends Clock {

    private Instant instant = OCT_8.atStartOfDay(ZoneOffset.UTC).toInstant();

    void moveTo(LocalDate day) {
      instant = day.atStartOfDay(ZoneOffset.UTC).toInstant().plus(Duration.ofHours(6));
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return instant;
    }
  }
}
