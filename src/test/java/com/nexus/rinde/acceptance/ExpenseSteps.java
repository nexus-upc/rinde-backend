package com.nexus.rinde.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.rinde.support.ExpenseApi;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Y;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.test.web.servlet.MockMvc;

/** Pasos de aceptación BDD para el bounded context Expense & Evidence. */
public class ExpenseSteps {

  private final ScenarioContext context;
  private final ExpenseApi api;

  public ExpenseSteps(ScenarioContext context, MockMvc mockMvc, ObjectMapper objectMapper) {
    this.context = context;
    this.api = new ExpenseApi(mockMvc, objectMapper);
  }

  @Cuando(
      "el conductor registra un gasto de tipo {string} por el monto {string} en fecha {string}"
          + " con evidencia {string} y clave {string} en el viaje {string}")
  public void driverRegistersExpenseWithEvidence(
      String category,
      String amount,
      String date,
      String evidenceUrl,
      String idempotencyKey,
      String tripName)
      throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf("luis@andes.pe");
    context.record(
        api.register(
            token,
            tripId,
            category,
            new BigDecimal(amount),
            "PEN",
            LocalDate.parse(date),
            idempotencyKey,
            evidenceUrl,
            2048L));
  }

  @Cuando(
      "el conductor registra un gasto de tipo {string} por el monto {string} en fecha {string}"
          + " sin evidencia y clave {string} en el viaje {string}")
  public void driverRegistersExpenseWithoutEvidence(
      String category, String amount, String date, String idempotencyKey, String tripName)
      throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf("luis@andes.pe");
    context.record(
        api.register(
            token,
            tripId,
            category,
            new BigDecimal(amount),
            "PEN",
            LocalDate.parse(date),
            idempotencyKey,
            null,
            null));
  }

  @Cuando("el conductor intenta registrar un gasto con monto {string} en el viaje {string}")
  public void driverAttemptsInvalidAmount(String amount, String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf("luis@andes.pe");
    context.record(
        api.register(
            token,
            tripId,
            "FUEL",
            new BigDecimal(amount),
            "PEN",
            LocalDate.parse("2026-10-20"),
            "KEY-INVALID-" + System.nanoTime(),
            null,
            null));
  }

  @Dado(
      "que el conductor registró un gasto de tipo {string} por el monto {string} con clave"
          + " {string} en el viaje {string}")
  public void driverRegisteredExpense(
      String category, String amount, String idempotencyKey, String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf("luis@andes.pe");
    api.register(
        token,
        tripId,
        category,
        new BigDecimal(amount),
        "PEN",
        LocalDate.parse("2026-10-20"),
        idempotencyKey,
        null,
        null);
  }

  @Cuando(
      "el conductor intenta registrar un gasto de tipo {string} por el monto {string} con clave"
          + " {string} en el viaje {string}")
  public void driverAttemptsDuplicateExpense(
      String category, String amount, String idempotencyKey, String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf("luis@andes.pe");
    context.record(
        api.register(
            token,
            tripId,
            category,
            new BigDecimal(amount),
            "PEN",
            LocalDate.parse("2026-10-20"),
            idempotencyKey,
            null,
            null));
  }

  @Cuando("el conductor solicita un enlace firmado para subir su comprobante")
  public void driverRequestsPresignedUrl() throws Exception {
    String token = context.accessTokenOf("luis@andes.pe");
    context.record(api.presignedUrl(token));
  }

  @Cuando("una persona sin token registra un gasto en el viaje {string}")
  public void unauthenticatedRegistersExpense(String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    context.record(
        api.register(
            null,
            tripId,
            "FUEL",
            new BigDecimal("100.00"),
            "PEN",
            LocalDate.parse("2026-10-20"),
            "KEY-NOAUTH",
            null,
            null));
  }

  @Dado(
      "que el conductor registró un gasto {string} de tipo {string} por el monto {string} con"
          + " clave {string} en el viaje {string}")
  public void driverRegisteredExpenseNamed(
      String expenseName, String category, String amount, String idempotencyKey, String tripName)
      throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf("luis@andes.pe");
    var res =
        api.register(
            token,
            tripId,
            category,
            new BigDecimal(amount),
            "PEN",
            LocalDate.parse("2026-10-20"),
            idempotencyKey,
            null,
            null);
    JsonNode node = context.record(res).andReturn().getResponse().getContentAsString().isEmpty()
        ? null
        : context.body();
    if (node != null && node.has("id")) {
      context.rememberExpenseId(expenseName, node.get("id").asText());
    }
  }

  @Dado(
      "que el conductor registró un gasto {string} de tipo {string} por el monto {string} con"
          + " evidencia {string} y clave {string} en el viaje {string}")
  public void driverRegisteredExpenseNamedWithEvidence(
      String expenseName,
      String category,
      String amount,
      String evidenceUrl,
      String idempotencyKey,
      String tripName)
      throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf("luis@andes.pe");
    var res =
        api.register(
            token,
            tripId,
            category,
            new BigDecimal(amount),
            "PEN",
            LocalDate.parse("2026-10-20"),
            idempotencyKey,
            evidenceUrl,
            2048L);
    JsonNode node = context.record(res).andReturn().getResponse().getContentAsString().isEmpty()
        ? null
        : context.body();
    if (node != null && node.has("id")) {
      context.rememberExpenseId(expenseName, node.get("id").asText());
    }
  }

  @Cuando("el conductor consulta los gastos del viaje {string}")
  public void driverQueriesExpenses(String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf("luis@andes.pe");
    context.record(api.listByTrip(token, tripId));
  }

  @Y("la lista de gastos contiene {int} elementos")
  public void expenseListHasSize(int expected) throws Exception {
    assertThat(context.body().isArray()).isTrue();
    assertThat(context.body().size()).isEqualTo(expected);
  }

  @Cuando("el conductor consulta el detalle del gasto {string}")
  public void driverQueriesExpenseDetail(String expenseName) throws Exception {
    String expenseId = context.expenseIdOf(expenseName);
    String token = context.accessTokenOf("luis@andes.pe");
    context.record(api.detail(token, expenseId));
  }

  @Cuando("el administrador {string} consulta el detalle del gasto {string}")
  public void adminQueriesExpenseDetail(String adminEmail, String expenseName) throws Exception {
    String expenseId = context.expenseIdOf(expenseName);
    String token = context.accessTokenOf(adminEmail);
    context.record(api.detail(token, expenseId));
  }

  @Cuando("el administrador aprueba el gasto {string}")
  public void adminApprovesExpense(String expenseName) throws Exception {
    String expenseId = context.expenseIdOf(expenseName);
    String token = context.accessTokenOf("ana@andes.pe");
    context.record(api.updateStatus(token, expenseId, "APPROVED", null));
  }

  @Cuando("el administrador observa el gasto {string} con el motivo {string}")
  public void adminObservesExpense(String expenseName, String reason) throws Exception {
    String expenseId = context.expenseIdOf(expenseName);
    String token = context.accessTokenOf("ana@andes.pe");
    context.record(api.updateStatus(token, expenseId, "OBSERVED", reason));
  }

  @Cuando("el administrador intenta observar el gasto {string} sin motivo")
  public void adminObservesWithoutReason(String expenseName) throws Exception {
    String expenseId = context.expenseIdOf(expenseName);
    String token = context.accessTokenOf("ana@andes.pe");
    context.record(api.updateStatus(token, expenseId, "OBSERVED", ""));
  }

  @Cuando("el conductor {string} intenta aprobar el gasto {string}")
  public void driverAttemptsApprove(String driverEmail, String expenseName) throws Exception {
    String expenseId = context.expenseIdOf(expenseName);
    String token = context.accessTokenOf(driverEmail);
    context.record(api.updateStatus(token, expenseId, "APPROVED", null));
  }
}
