package com.nexus.rinde.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexus.rinde.support.FleetStub;
import com.nexus.rinde.trip.domain.model.valueobjects.MaintenanceState;
import io.cucumber.java.es.Dado;
import java.util.UUID;

/**
 * Pasos que preparan lo que responde Fleet: unidades, conductores, mantenimiento y habilitación.
 * Fleet real vive en otro servicio; aquí su respuesta se fija con el doble de WireMock.
 */
public class FleetSteps {

  private final ScenarioContext context;
  private final FleetStub fleet;

  public FleetSteps(ScenarioContext context, FleetStub fleet) {
    this.context = context;
    this.fleet = fleet;
  }

  @Dado("que existe el vehículo {string}")
  public void aVehicleExists(String name) {
    UUID vehicleId = UUID.randomUUID();
    context.rememberVehicleId(name, vehicleId.toString());
    fleet.registerVehicle(vehicleId);
  }

  @Dado(
      "que el administrador registró el vehículo {string} con placa {string}, marca {string}, modelo {string}, año {int} y capacidad {string}")
  public void theAdministratorRegisteredVehicle(
      String name, String plate, String brand, String model, int year, String capacity) {
    aVehicleExists(name);
  }

  @Dado("que el vehículo {string} no está registrado en Fleet")
  public void aVehicleIsNotRegisteredInFleet(String name) {
    context.rememberVehicleId(name, UUID.randomUUID().toString());
    fleet.useStrictRegistry();
  }

  /** Solo Fleet conoce a los conductores registrados; los demás responden como no existentes. */
  @Dado("que la asignación consulta a Fleet sin usar el doble de prueba")
  public void assignmentQueriesFleetWithoutDouble() {
    fleet.useStrictRegistry();
  }

  @Dado(
      "que el administrador registró el conductor {string} con documento {string} {string}, licencia {string} categoría {string} y vencimiento {string} vinculado al usuario {string}")
  public void theAdministratorRegisteredDriver(
      String name,
      String documentType,
      String documentNumber,
      String licenseNumber,
      String licenseCategory,
      String expiresOn,
      String email) {
    UUID driverId = UUID.randomUUID();
    UUID userId = context.userIdOf(email);
    assertThat(userId).as("El usuario %s debe estar invitado", email).isNotNull();
    context.rememberDriverId(name, driverId.toString());
    fleet.registerDriver(driverId, userId);
  }

  @Dado("que Fleet informa mantenimiento al día")
  public void fleetReportsMaintenanceUpToDate() {
    fleet.setMaintenance(MaintenanceState.UP_TO_DATE);
  }

  @Dado("que Fleet informa mantenimiento próximo")
  public void fleetReportsMaintenanceDueSoon() {
    fleet.setMaintenance(MaintenanceState.DUE_SOON);
  }

  @Dado("que Fleet informa mantenimiento vencido")
  public void fleetReportsMaintenanceOverdue() {
    fleet.setMaintenance(MaintenanceState.OVERDUE);
  }

  @Dado("que Fleet informa que el conductor no está habilitado")
  public void fleetReportsDriverDisabled() {
    fleet.setDriverEnabled(false);
  }
}
