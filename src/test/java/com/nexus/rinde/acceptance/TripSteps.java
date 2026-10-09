package com.nexus.rinde.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.rinde.settlement.interfaces.acl.SettlementClosed;
import com.nexus.rinde.support.TestFleetAvailabilityService;
import com.nexus.rinde.support.TripApi;
import com.nexus.rinde.trip.domain.model.valueobjects.MaintenanceState;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Y;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.web.servlet.MockMvc;

/** Pasos de aceptación para programar, consultar y avanzar viajes. */
public class TripSteps {

  private final ScenarioContext context;
  private final TestFleetAvailabilityService fleet;
  private final ApplicationEventPublisher publisher;
  private final TripApi api;

  public TripSteps(
      ScenarioContext context,
      TestFleetAvailabilityService fleet,
      ApplicationEventPublisher publisher,
      MockMvc mockMvc,
      ObjectMapper objectMapper) {
    this.context = context;
    this.fleet = fleet;
    this.publisher = publisher;
    this.api = new TripApi(mockMvc, objectMapper);
  }

  @Before
  public void resetFleet() {
    fleet.reset();
  }

  @Dado("que existe el vehículo {string}")
  public void aVehicleExists(String name) {
    context.rememberVehicleId(name, UUID.randomUUID().toString());
  }

  @Dado("que el vehículo {string} no está registrado en Fleet")
  public void aVehicleIsNotRegisteredInFleet(String name) {
    fleet.useRealAdapter(true);
    context.rememberVehicleId(name, UUID.randomUUID().toString());
  }

  @Dado("que la asignación consulta a Fleet sin usar el doble de prueba")
  public void assignmentQueriesFleetDirectly() {
    fleet.useRealAdapter(true);
  }

  @Dado("que la liquidación del viaje {string} de la empresa con RUC {string} fue cerrada")
  public void theSettlementWasClosed(String name, String ruc) {
    publisher.publishEvent(
        new SettlementClosed(
            UUID.randomUUID(),
            Instant.now(),
            UUID.fromString(context.tenantIdOf(ruc)),
            UUID.fromString(context.tripIdOf(name)),
            UUID.randomUUID()));
  }

  @Dado("que Fleet informa mantenimiento al día")
  public void fleetReportsMaintenanceUpToDate() {
    fleet.setMaintenanceState(MaintenanceState.UP_TO_DATE);
  }

  @Dado("que Fleet informa mantenimiento próximo")
  public void fleetReportsMaintenanceDueSoon() {
    fleet.setMaintenanceState(MaintenanceState.DUE_SOON);
  }

  @Dado("que Fleet informa mantenimiento vencido")
  public void fleetReportsMaintenanceOverdue() {
    fleet.setMaintenanceState(MaintenanceState.OVERDUE);
  }

  @Dado("que Fleet informa que el conductor no está habilitado")
  public void fleetReportsDriverDisabled() {
    fleet.setDriverEnabled(false);
  }

  @Dado(
      "que el administrador programa el viaje {string} de {string} a {string} con carga {string} y salida {string}")
  @Cuando(
      "el administrador programa el viaje {string} de {string} a {string} con carga {string} y salida {string}")
  public void theAdministratorSchedulesTrip(
      String name, String origin, String destination, String cargo, String departureDate)
      throws Exception {
    schedule(name, origin, destination, cargo, null, departureDate, false);
  }

  @Cuando(
      "el administrador programa el viaje {string} de {string} a {string} con carga {string}, peso {string} y salida {string}")
  public void theAdministratorSchedulesTripWithWeight(
      String name,
      String origin,
      String destination,
      String cargo,
      String weight,
      String departureDate)
      throws Exception {
    schedule(name, origin, destination, cargo, new BigDecimal(weight), departureDate, false);
  }

  @Cuando("el administrador programa el viaje {string} con salida pasada {string}")
  public void theAdministratorSchedulesPastTrip(String name, String departureDate)
      throws Exception {
    schedule(name, "Lima", "Arequipa", "Carga confirmada", null, departureDate, false);
  }

  @Cuando("el administrador confirma la fecha pasada y programa el viaje {string} para {string}")
  public void theAdministratorConfirmsPastDateAndSchedules(String name, String departureDate)
      throws Exception {
    schedule(name, "Lima", "Arequipa", "Carga ya ejecutada", null, departureDate, true);
  }

  @Cuando("el administrador intenta programar un viaje sin destino")
  public void theAdministratorSchedulesWithoutDestination() throws Exception {
    context.record(
        api.scheduleRaw(
            context.currentAdministratorToken(),
            Map.of(
                "origin", "Lima",
                "cargoDescription", "Repuestos",
                "departureDate", "2026-10-20")));
  }

  @Cuando("el administrador intenta programar un viaje con peso {string}")
  public void theAdministratorSchedulesWithWeight(String weight) throws Exception {
    schedule(
        "peso inválido",
        "Lima",
        "Arequipa",
        "Repuestos",
        new BigDecimal(weight),
        "2026-10-20",
        false);
  }

  @Cuando("una persona sin token programa un viaje")
  public void anUnauthenticatedPersonSchedulesTrip() throws Exception {
    context.record(
        api.schedule(
            null, "Lima", "Arequipa", "Repuestos", null, LocalDate.parse("2026-10-20"), false));
  }

  @Cuando("el conductor {string} intenta programar un viaje")
  public void theDriverTriesToSchedule(String driverEmail) throws Exception {
    context.record(
        api.schedule(
            context.accessTokenOf(driverEmail),
            "Lima",
            "Arequipa",
            "Repuestos",
            null,
            LocalDate.parse("2026-10-20"),
            false));
  }

  @Dado("que el administrador asigna el viaje {string} al vehículo {string} y al conductor {string}")
  @Cuando("el administrador asigna el viaje {string} al vehículo {string} y al conductor {string}")
  public void theAdministratorAssigns(String tripName, String vehicle, String driverEmail)
      throws Exception {
    assign(
        context.currentAdministratorToken(), tripName, vehicle, driverEmail, false);
  }

  @Cuando(
      "el administrador confirma el mantenimiento vencido y asigna el viaje {string} al vehículo {string} y al conductor {string}")
  public void theAdministratorConfirmsOverdueMaintenance(
      String tripName, String vehicle, String driverEmail) throws Exception {
    assign(
        context.currentAdministratorToken(), tripName, vehicle, driverEmail, true);
  }

  /** Asigna usando los ids que devuelve Fleet, sin pasar por el doble de prueba. */
  @Dado("que el administrador asigna el viaje {string} con el vehículo de Fleet {string} y el conductor de Fleet {string}")
  @Cuando("el administrador asigna el viaje {string} con el vehículo de Fleet {string} y el conductor de Fleet {string}")
  public void theAdministratorAssignsWithFleetIds(
      String tripName, String vehicleName, String driverName) throws Exception {
    context.record(
        api.assign(
            context.currentAdministratorToken(),
            context.tripIdOf(tripName),
            context.vehicleIdOf(vehicleName),
            context.driverIdOf(driverName),
            false));
  }

  @Cuando("el conductor {string} intenta asignar el viaje {string}")
  public void theDriverTriesToAssign(String driverEmail, String tripName) throws Exception {
    context.record(
        api.assign(
            context.accessTokenOf(driverEmail),
            context.tripIdOf(tripName),
            UUID.randomUUID().toString(),
            context.userIdOf(driverEmail),
            false));
  }

  @Dado("que el viaje {string} ya fue asignado al vehículo {string} y al conductor {string}")
  public void theTripWasAssigned(String tripName, String vehicle, String driverEmail)
      throws Exception {
    assign(
        context.currentAdministratorToken(), tripName, vehicle, driverEmail, false);
    assertThat(context.statusCode()).isEqualTo(200);
  }

  @Cuando("el administrador consulta el tablero sin filtro")
  public void theAdministratorViewsAllTrips() throws Exception {
    context.record(api.list(context.currentAdministratorToken(), null));
  }

  @Cuando("el conductor {string} consulta el tablero")
  public void theDriverViewsTheBoard(String email) throws Exception {
    context.record(api.list(context.accessTokenOf(email), null));
  }

  @Cuando("el administrador consulta el tablero con estados {string}")
  public void theAdministratorViewsTripsByStatus(String statuses) throws Exception {
    context.record(api.list(context.currentAdministratorToken(), statuses));
  }

  @Cuando("el usuario {string} consulta el detalle del viaje {string}")
  public void aUserViewsTripDetails(String email, String tripName) throws Exception {
    context.record(
        api.detail(context.accessTokenOf(email), context.tripIdOf(tripName)));
  }

  @Cuando("el administrador consulta el detalle del viaje {string}")
  public void theAdministratorViewsTripDetails(String tripName) throws Exception {
    context.record(
        api.detail(context.currentAdministratorToken(), context.tripIdOf(tripName)));
  }

  @Cuando("el conductor {string} consulta sus viajes asignados")
  public void theDriverViewsAssignedTrips(String email) throws Exception {
    context.record(api.assignedToMe(context.accessTokenOf(email)));
  }

  @Cuando("el administrador consulta sus viajes asignados")
  public void theAdministratorViewsAssignedTrips() throws Exception {
    context.record(api.assignedToMe(context.currentAdministratorToken()));
  }

  @Dado("que el conductor {string} inicia el viaje {string}")
  @Cuando("el conductor {string} inicia el viaje {string}")
  public void theDriverStartsTrip(String email, String tripName) throws Exception {
    context.record(api.start(context.accessTokenOf(email), context.tripIdOf(tripName)));
  }

  @Dado("que el conductor inicia el viaje {string}")
  public void theDriverStartsTripDefault(String tripName) throws Exception {
    theDriverStartsTrip("luis@andes.pe", tripName);
  }

  @Cuando("el conductor {string} intenta iniciar el viaje {string}")
  public void anotherDriverTriesToStartTrip(String email, String tripName) throws Exception {
    context.record(api.start(context.accessTokenOf(email), context.tripIdOf(tripName)));
  }

  @Dado("que el conductor {string} finaliza el viaje {string}")
  @Cuando("el conductor {string} finaliza el viaje {string}")
  public void theDriverFinishesTrip(String email, String tripName) throws Exception {
    context.record(api.finish(context.accessTokenOf(email), context.tripIdOf(tripName)));
  }

  @Cuando("el administrador intenta iniciar el viaje {string}")
  public void theAdministratorTriesToStartTrip(String tripName) throws Exception {
    context.record(
        api.start(context.currentAdministratorToken(), context.tripIdOf(tripName)));
  }

  @Cuando("un administrador de otra empresa consulta el viaje {string}")
  public void anotherCompanyAdministratorViewsTrip(String tripName) throws Exception {
    context.record(
        api.detail(context.currentAdministratorToken(), context.tripIdOf(tripName)));
  }

  @Cuando("un administrador de otra empresa asigna el viaje {string}")
  public void anotherCompanyAdministratorAssignsTrip(String tripName) throws Exception {
    context.record(
        api.assign(
            context.currentAdministratorToken(),
            context.tripIdOf(tripName),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            false));
  }

  @Cuando("el administrador consulta un viaje que no existe")
  public void theAdministratorViewsUnknownTrip() throws Exception {
    context.record(
        api.detail(context.currentAdministratorToken(), UUID.randomUUID().toString()));
  }

  @Cuando("una persona sin token consulta el detalle del viaje {string}")
  public void anUnauthenticatedPersonViewsTrip(String tripName) throws Exception {
    context.record(api.detail(null, context.tripIdOf(tripName)));
  }

  @Y("el viaje queda en estado {string}")
  public void theTripHasStatus(String status) throws Exception {
    assertThat(context.body().get("status").asText()).isEqualTo(status);
  }

  @Y("la lista contiene {int} viajes")
  public void theTripListHasSize(int expected) throws Exception {
    assertThat(context.body().size()).isEqualTo(expected);
  }

  @Y("la lista incluye el viaje {string}")
  public void theTripListIncludesTrip(String tripName) throws Exception {
    List<String> ids =
        StreamSupport.stream(context.body().spliterator(), false)
            .map(trip -> trip.get("id").asText())
            .toList();
    assertThat(ids).contains(context.tripIdOf(tripName));
  }

  @Y("la lista está vacía")
  public void theTripListIsEmpty() throws Exception {
    assertThat(context.body()).isEmpty();
  }

  @Y("la lista incluye el código {string}")
  public void theTripListIncludesCode(String code) throws Exception {
    assertThat(codesInList()).contains(code);
  }

  @Y("la lista no incluye el código {string}")
  public void theTripListExcludesCode(String code) throws Exception {
    assertThat(codesInList()).doesNotContain(code);
  }

  @Y("el tablero devuelve los códigos en orden {string}")
  public void theBoardReturnsCodesInOrder(String codes) throws Exception {
    assertThat(codesInList()).containsExactly(codes.split(","));
  }

  @Y("el historial contiene los estados {string}")
  public void theHistoryHasStatuses(String statuses) throws Exception {
    List<String> actual =
        StreamSupport.stream(context.body().get("statusChanges").spliterator(), false)
            .map(change -> change.get("status").asText())
            .toList();
    assertThat(actual).containsExactly(statuses.split(","));
  }

  @Y("la respuesta incluye {int} cambios de estado")
  public void theResponseHasStatusChanges(int count) throws Exception {
    assertThat(context.body().get("statusChanges").size()).isEqualTo(count);
  }

  @Y("el peso de la carga es {string}")
  public void theCargoWeightIs(String weight) throws Exception {
    assertThat(context.body().get("cargoWeightKg").decimalValue())
        .isEqualByComparingTo(new BigDecimal(weight));
  }

  @Y("la respuesta incluye una alerta de mantenimiento {string}")
  public void theResponseIncludesMaintenanceAlert(String alert) throws Exception {
    assertThat(context.body().get("maintenanceAlert").asText()).isEqualTo(alert);
  }

  @Y("la respuesta no incluye una alerta de mantenimiento")
  public void theResponseHasNoMaintenanceAlert() throws Exception {
    JsonNode alert = context.body().get("maintenanceAlert");
    assertThat(alert == null || alert.isNull()).isTrue();
  }

  @Y("el viaje no tiene vehículo ni conductor asignados")
  public void theTripHasNoAssignedResources() throws Exception {
    assertThat(context.body().get("vehicleId").isNull()).isTrue();
    assertThat(context.body().get("driverId").isNull()).isTrue();
  }

  @Y("la fecha de inicio y fin están registradas")
  public void theStartAndFinishDatesAreSet() throws Exception {
    assertThat(context.body().get("startedAt").isNull()).isFalse();
    assertThat(context.body().get("finishedAt").isNull()).isFalse();
  }

  @Y("el detalle registra quién confirmó el mantenimiento vencido")
  public void theDetailShowsOverdueConfirmation() throws Exception {
    assertThat(context.body().get("overdueMaintenanceConfirmedBy").isNull()).isFalse();
  }

  @Y("el detalle muestra la fecha de asignación")
  public void theDetailShowsAssignmentDate() throws Exception {
    assertThat(context.body().get("assignedAt").isNull()).isFalse();
  }

  @Y("la respuesta no incluye el campo {string}")
  public void theResponseDoesNotIncludeField(String field) throws Exception {
    assertThat(context.body().has(field)).isFalse();
  }

  private void schedule(
      String name,
      String origin,
      String destination,
      String cargo,
      BigDecimal weight,
      String departureDate,
      boolean confirmPastDate)
      throws Exception {
    context.record(
        api.schedule(
            context.currentAdministratorToken(),
            origin,
            destination,
            cargo,
            weight,
            LocalDate.parse(departureDate),
            confirmPastDate));
    if (context.statusCode() == 201) {
      context.rememberTripId(name, context.body().get("id").asText());
    }
  }

  private void assign(
      String token,
      String tripName,
      String vehicleName,
      String driverEmail,
      boolean confirmOverdue)
      throws Exception {
    context.record(
        api.assign(
            token,
            context.tripIdOf(tripName),
            context.vehicleIdOf(vehicleName),
            context.userIdOf(driverEmail),
            confirmOverdue));
  }

  private List<String> codesInList() throws Exception {
    return StreamSupport.stream(context.body().spliterator(), false)
        .map(trip -> trip.get("code").asText())
        .toList();
  }
}
