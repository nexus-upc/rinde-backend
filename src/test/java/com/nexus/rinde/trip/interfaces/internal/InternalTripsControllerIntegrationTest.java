package com.nexus.rinde.trip.interfaces.internal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexus.rinde.support.AbstractIntegrationTest;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Consultas internas de viajes que usan Expense, Dashboard y Settlement. */
class InternalTripsControllerIntegrationTest extends AbstractIntegrationTest {

  private static final String ADMIN_EMAIL = "ana@andes.pe";

  @Test
  void listsTheTripsOfTheTenant() throws Exception {
    UUID tenantId = UUID.randomUUID();
    UUID tripId = scheduleTrip(tenantId);

    mockMvc
        .perform(get("/api/internal/v1/trips/tenants/{tenantId}", tenantId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].tripId").value(tripId.toString()))
        .andExpect(jsonPath("$[0].code").value("TRP-000001"))
        .andExpect(jsonPath("$[0].status").value("SCHEDULED"))
        .andExpect(jsonPath("$[0].origin").value("Lima"));
  }

  @Test
  void returnsTheSummaryOfATripOfTheTenant() throws Exception {
    UUID tenantId = UUID.randomUUID();
    UUID tripId = scheduleTrip(tenantId);

    mockMvc
        .perform(get("/api/internal/v1/trips/tenants/{tenantId}/{tripId}", tenantId, tripId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tripId").value(tripId.toString()))
        .andExpect(jsonPath("$.destination").value("Arequipa"))
        .andExpect(jsonPath("$.cargoDescription").value("Repuestos"))
        .andExpect(jsonPath("$.statusChanges").doesNotExist());
  }

  @Test
  void returnsOnlyTheStatusOfATrip() throws Exception {
    UUID tenantId = UUID.randomUUID();
    UUID tripId = scheduleTrip(tenantId);

    mockMvc
        .perform(get("/api/internal/v1/trips/tenants/{tenantId}/{tripId}/status", tenantId, tripId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tripId").value(tripId.toString()))
        .andExpect(jsonPath("$.status").value("SCHEDULED"));
  }

  @Test
  void answersNotFoundForATripOfAnotherTenant() throws Exception {
    UUID tripId = scheduleTrip(UUID.randomUUID());

    mockMvc
        .perform(
            get(
                "/api/internal/v1/trips/tenants/{tenantId}/{tripId}/status",
                UUID.randomUUID(),
                tripId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("No existe el viaje en la empresa."));
  }

  @Test
  void documentsTheInternalEndpointsWithTheInternalTag() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.paths['/api/internal/v1/trips/tenants/{tenantId}'].get.tags[0]")
                .value("Internal"));
  }

  /** Programa un viaje con un administrador de la empresa y devuelve su identificador. */
  private UUID scheduleTrip(UUID tenantId) throws Exception {
    String token = tokens.issue(UUID.randomUUID(), tenantId, "ADMINISTRATOR", ADMIN_EMAIL);
    String response =
        api.schedule(token, "Lima", "Arequipa", "Repuestos", null, LocalDate.now().plusDays(5), false)
            .andReturn()
            .getResponse()
            .getContentAsString();
    return UUID.fromString(objectMapper.readTree(response).get("id").asText());
  }
}
