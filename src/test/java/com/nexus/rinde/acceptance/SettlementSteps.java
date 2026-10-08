package com.nexus.rinde.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.rinde.support.SettlementApi;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Y;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.test.web.servlet.MockMvc;

/** Pasos de aceptación BDD para el bounded context Settlement. */
public class SettlementSteps {

  private final ScenarioContext context;
  private final SettlementApi api;

  public SettlementSteps(ScenarioContext context, MockMvc mockMvc, ObjectMapper objectMapper) {
    this.context = context;
    this.api = new SettlementApi(mockMvc, objectMapper);
  }

  @Dado("que existe la liquidación {string} del viaje {string}")
  public void settlementExistsForTrip(String settlementName, String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.currentAdministratorToken();
    var res = api.getByTrip(token, tripId);
    JsonNode node = context.record(res).andReturn().getResponse().getContentAsString().isEmpty()
        ? null
        : context.body();
    if (node != null && node.has("id")) {
      context.rememberSettlementId(settlementName, node.get("id").asText());
    }
  }

  @Cuando("el administrador registra un anticipo de {string} soles para el viaje {string}")
  public void adminRegistersAdvance(String amount, String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.currentAdministratorToken();
    context.record(api.registerAdvance(token, tripId, new BigDecimal(amount), "PEN"));
  }

  @Cuando("el administrador intenta registrar un anticipo de {string} soles para el viaje {string}")
  public void adminTriesToRegisterAdvance(String amount, String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.currentAdministratorToken();
    context.record(api.registerAdvance(token, tripId, new BigDecimal(amount), "PEN"));
  }

  @Cuando("el administrador intenta registrar un anticipo para un viaje inexistente")
  public void adminRegistersAdvanceForUnknownTrip() throws Exception {
    String token = context.currentAdministratorToken();
    context.record(
        api.registerAdvance(token, UUID.randomUUID().toString(), new BigDecimal("100.00"), "PEN"));
  }

  @Cuando("el conductor {string} intenta registrar un anticipo de {string} soles para el viaje {string}")
  public void driverTriesToRegisterAdvance(String email, String amount, String tripName)
      throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf(email);
    context.record(api.registerAdvance(token, tripId, new BigDecimal(amount), "PEN"));
  }

  @Cuando("una persona sin token intenta registrar un anticipo para el viaje {string}")
  public void unauthenticatedTriesToRegisterAdvance(String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    context.record(api.registerAdvance(null, tripId, new BigDecimal("100.00"), "PEN"));
  }

  @Cuando("el administrador consulta la liquidación {string}")
  public void adminQueriesSettlement(String settlementName) throws Exception {
    String settlementId = context.settlementIdOf(settlementName);
    String token = context.currentAdministratorToken();
    context.record(api.getById(token, settlementId));
  }

  @Cuando("el administrador consulta una liquidación con id inexistente")
  public void adminQueriesUnknownSettlement() throws Exception {
    String token = context.currentAdministratorToken();
    context.record(api.getById(token, UUID.randomUUID().toString()));
  }

  @Cuando("el conductor {string} consulta la liquidación {string}")
  public void driverQueriesSettlement(String email, String settlementName) throws Exception {
    String settlementId = context.settlementIdOf(settlementName);
    String token = context.accessTokenOf(email);
    context.record(api.getById(token, settlementId));
  }

  @Cuando("una persona sin token consulta la liquidación {string}")
  public void unauthenticatedQueriesSettlement(String settlementName) throws Exception {
    String settlementId = context.settlementIdOf(settlementName);
    context.record(api.getById(null, settlementId));
  }

  @Cuando("el administrador consulta la liquidación del viaje {string}")
  public void adminQueriesSettlementByTrip(String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.currentAdministratorToken();
    context.record(api.getByTrip(token, tripId));
  }

  @Cuando("el administrador consulta la liquidación de un viaje sin liquidación")
  public void adminQueriesSettlementForUnknownTrip() throws Exception {
    String token = context.currentAdministratorToken();
    context.record(api.getByTrip(token, UUID.randomUUID().toString()));
  }

  @Cuando("el conductor {string} consulta la liquidación del viaje {string}")
  public void driverQueriesSettlementByTrip(String email, String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf(email);
    context.record(api.getByTrip(token, tripId));
  }

  @Cuando("una persona sin token consulta la liquidación del viaje {string}")
  public void unauthenticatedQueriesSettlementByTrip(String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    context.record(api.getByTrip(null, tripId));
  }

  @Y("la respuesta incluye el campo {string} con el id del viaje {string}")
  public void responseContainsTripId(String field, String tripName) throws Exception {
    String expectedTripId = context.tripIdOf(tripName);
    assertThat(context.body().get(field).asText()).isEqualTo(expectedTripId);
  }
}
