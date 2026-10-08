package com.nexus.rinde.acceptance;

import com.nexus.rinde.support.DashboardApi;
import io.cucumber.java.es.Cuando;
import java.util.UUID;
import org.springframework.test.web.servlet.MockMvc;

/** Pasos de aceptación BDD para el bounded context Dashboard. */
public class DashboardSteps {

  private final ScenarioContext context;
  private final DashboardApi api;

  public DashboardSteps(ScenarioContext context, MockMvc mockMvc) {
    this.context = context;
    this.api = new DashboardApi(mockMvc);
  }

  @Cuando("el administrador consulta el resumen del viaje {string}")
  public void adminQueriesTripSummary(String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.currentAdministratorToken();
    context.record(api.getTripSummary(token, tripId));
  }

  @Cuando("el administrador consulta el resumen de un viaje inexistente")
  public void adminQueriesUnknownTripSummary() throws Exception {
    String token = context.currentAdministratorToken();
    context.record(api.getTripSummary(token, UUID.randomUUID().toString()));
  }

  @Cuando("una persona sin token consulta el resumen del viaje {string}")
  public void unauthenticatedQueriesTripSummary(String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    context.record(api.getTripSummary(null, tripId));
  }

  @Cuando("el administrador consulta el estado de la flota")
  public void adminQueriesFleetStatus() throws Exception {
    String token = context.currentAdministratorToken();
    context.record(api.getFleetStatus(token));
  }

  @Cuando("una persona sin token consulta el estado de la flota")
  public void unauthenticatedQueriesFleetStatus() throws Exception {
    context.record(api.getFleetStatus(null));
  }

  @Cuando("el administrador consulta las métricas operativas")
  public void adminQueriesMetrics() throws Exception {
    String token = context.currentAdministratorToken();
    context.record(api.getMetrics(token));
  }

  @Cuando("una persona sin token consulta las métricas operativas")
  public void unauthenticatedQueriesMetrics() throws Exception {
    context.record(api.getMetrics(null));
  }
}
