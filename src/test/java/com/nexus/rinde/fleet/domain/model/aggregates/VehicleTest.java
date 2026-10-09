package com.nexus.rinde.fleet.domain.model.aggregates;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexus.rinde.fleet.domain.model.valueobjects.VehicleStatus;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Prueba las transiciones de estado de la unidad ligadas al viaje. */
class VehicleTest {

  @Test
  void startTripMovesAvailableVehicleToInTrip() {
    Vehicle vehicle = newVehicle();

    vehicle.startTrip();

    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.IN_TRIP);
  }

  @Test
  void finishTripMovesInTripVehicleBackToAvailable() {
    Vehicle vehicle = newVehicle();
    vehicle.startTrip();

    vehicle.finishTrip();

    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
  }

  @Test
  void startTripLeavesVehicleInMaintenanceUntouched() {
    Vehicle vehicle = newVehicle();
    vehicle.sendToMaintenance();

    vehicle.startTrip();

    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.IN_MAINTENANCE);
  }

  @Test
  void finishTripLeavesVehicleInMaintenanceUntouched() {
    Vehicle vehicle = newVehicle();
    vehicle.sendToMaintenance();

    vehicle.finishTrip();

    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.IN_MAINTENANCE);
  }

  private Vehicle newVehicle() {
    return new Vehicle(
        UUID.randomUUID(), "ABC-123", "Volvo", "FH", 2022, new BigDecimal("5000"));
  }
}
