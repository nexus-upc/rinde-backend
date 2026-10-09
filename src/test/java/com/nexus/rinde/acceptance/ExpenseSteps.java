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

  @Dado(
      "que el conductor {string} registró el gasto {string} de tipo {string} por el monto {string}"
          + " con clave {string} en el viaje {string}")
  public void namedDriverRegistersExpense(
      String email,
      String expenseName,
      String category,
      String amount,
      String idempotencyKey,
      String tripName)
      throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf(email);
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
    assertThat(context.statusCode()).isEqualTo(201);
    context.rememberExpenseId(expenseName, context.body().get("id").asText());
  }

  @Cuando(
      "el conductor {string} intenta registrar un gasto de tipo {string} por el monto {string}"
          + " con clave {string} en el viaje {string}")
  public void namedDriverAttemptsExpense(
      String email, String category, String amount, String idempotencyKey, String tripName)
      throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf(email);
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

  @Cuando(
      "el conductor intenta registrar un gasto de tipo {string} por el monto {string} en un viaje"
          + " que no existe")
  public void driverAttemptsExpenseOnUnknownTrip(String category, String amount)
      throws Exception {
    String token = context.accessTokenOf("luis@andes.pe");
    context.record(
        api.register(
            token,
            java.util.UUID.randomUUID().toString(),
            category,
            new BigDecimal(amount),
            "PEN",
            LocalDate.parse("2026-10-20"),
            "KEY-UNKNOWN-" + System.nanoTime(),
            null,
            null));
  }

  @Cuando(
      "el conductor sincroniza un lote con un gasto para el viaje {string} y otro para un viaje que"
          + " no existe")
  public void driverSyncsBatchWithUnknownTrip(String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf("luis@andes.pe");
    java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
    items.add(syncItem(tripId, "KEY-SYNC-VALIDO-" + System.nanoTime()));
    items.add(
        syncItem(
            java.util.UUID.randomUUID().toString(),
            "KEY-SYNC-INEXISTENTE-" + System.nanoTime()));
    context.record(api.sync(token, items));
  }

  @Cuando(
      "el conductor sincroniza un lote con un gasto para el viaje {string} y otro para el viaje"
          + " {string}")
  public void driverSyncsBatchWithTwoInvalidTrips(String firstTrip, String secondTrip)
      throws Exception {
    String token = context.accessTokenOf("luis@andes.pe");
    java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
    items.add(syncItem(tripIdOrRandom(firstTrip), "KEY-TODOS-INVALIDOS-1-" + System.nanoTime()));
    items.add(syncItem(tripIdOrRandom(secondTrip), "KEY-TODOS-INVALIDOS-2-" + System.nanoTime()));
    context.record(api.sync(token, items));
  }

  @Cuando(
      "el conductor sincroniza el lote con las claves {string} y {string} para el viaje {string}")
  public void driverSyncsBatchWithKeys(String firstKey, String secondKey, String tripName)
      throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf("luis@andes.pe");
    java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
    items.add(syncItem(tripId, firstKey));
    items.add(syncItem(tripId, secondKey));
    context.record(api.sync(token, items));
  }

  @Y("la respuesta no incluye el gasto {string}")
  public void responseDoesNotIncludeExpense(String expenseName) throws Exception {
    assertThat(context.lastResponseBody()).doesNotContain(context.expenseIdOf(expenseName));
  }

  /** Un viaje no existente se identifica con un UUID aleatorio. */
  private String tripIdOrRandom(String tripName) {
    String tripId = context.tripIdOf(tripName);
    return tripId != null ? tripId : java.util.UUID.randomUUID().toString();
  }

  private static java.util.Map<String, Object> syncItem(String tripId, String idempotencyKey) {
    java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
    item.put("tripId", tripId);
    item.put("category", "FOOD");
    item.put("amount", new java.math.BigDecimal("25.00"));
    item.put("currency", "PEN");
    item.put("expenseDate", "2026-10-20");
    item.put("idempotencyKey", idempotencyKey);
    return item;
  }

  @Dado("que el administrador {string} aprobó el gasto {string}")
  public void namedAdministratorApprovesExpense(String email, String expenseName) throws Exception {
    String expenseId = context.expenseIdOf(expenseName);
    String token = context.accessTokenOf(email);
    context.record(api.updateStatus(token, expenseId, "APPROVED", null));
    assertThat(context.statusCode()).isEqualTo(200);
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

  @Y("el elemento {int} de rechazados tiene el campo {string} con el valor {string}")
  public void rejectedItemHasField(int index, String field, String expected) throws Exception {
    assertThat(context.body().get("rejected").get(index).get(field).asText()).isEqualTo(expected);
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

  @Cuando("el conductor sincroniza un lote con {int} gastos para el viaje {string}")
  public void driverSyncsBatch(int count, String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf("luis@andes.pe");
    java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
    for (int i = 1; i <= count; i++) {
      java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
      item.put("tripId", tripId);
      item.put("category", "FOOD");
      item.put("amount", new java.math.BigDecimal("25.00"));
      item.put("currency", "PEN");
      item.put("expenseDate", "2026-10-20");
      item.put("idempotencyKey", "KEY-SYNC-" + i + "-" + System.nanoTime());
      items.add(item);
    }
    context.record(api.sync(token, items));
  }

  @Cuando(
      "el conductor sincroniza un lote que incluye el gasto con clave {string} y uno nuevo con"
          + " clave {string} para el viaje {string}")
  public void driverSyncsMixedBatch(String existingKey, String newKey, String tripName)
      throws Exception {
    String tripId = context.tripIdOf(tripName);
    String token = context.accessTokenOf("luis@andes.pe");
    java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();

    java.util.Map<String, Object> item1 = new java.util.LinkedHashMap<>();
    item1.put("tripId", tripId);
    item1.put("category", "FUEL");
    item1.put("amount", new java.math.BigDecimal("100.00"));
    item1.put("currency", "PEN");
    item1.put("expenseDate", "2026-10-20");
    item1.put("idempotencyKey", existingKey);
    items.add(item1);

    java.util.Map<String, Object> item2 = new java.util.LinkedHashMap<>();
    item2.put("tripId", tripId);
    item2.put("category", "TOLL");
    item2.put("amount", new java.math.BigDecimal("15.00"));
    item2.put("currency", "PEN");
    item2.put("expenseDate", "2026-10-20");
    item2.put("idempotencyKey", newKey);
    items.add(item2);

    context.record(api.sync(token, items));
  }

  @Cuando("una persona sin token sincroniza un lote de gastos para el viaje {string}")
  public void unauthenticatedSyncsBatch(String tripName) throws Exception {
    String tripId = context.tripIdOf(tripName);
    java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
    item.put("tripId", tripId);
    item.put("category", "FUEL");
    item.put("amount", new java.math.BigDecimal("50.00"));
    item.put("currency", "PEN");
    item.put("expenseDate", "2026-10-20");
    item.put("idempotencyKey", "KEY-NOAUTH-SYNC");
    context.record(api.sync(null, java.util.List.of(item)));
  }
}
