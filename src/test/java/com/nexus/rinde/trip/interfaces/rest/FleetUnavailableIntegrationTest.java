package com.nexus.rinde.trip.interfaces.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexus.rinde.support.AbstractIntegrationTest;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Con Fleet caído, asignar responde 503 sin cambiar el viaje; lo que no depende de Fleet sigue. */
class FleetUnavailableIntegrationTest extends AbstractIntegrationTest {

  private static final String ADMIN_EMAIL = "ana@andes.pe";
  private static final String FLEET_DOWN_MESSAGE =
      "El servicio de flota no está disponible en este momento. Intente nuevamente más tarde.";

  @Test
  void assignmentAnswersServiceUnavailableAndKeepsTheTripScheduled() throws Exception {
    UUID tenantId = UUID.randomUUID();
    String token = tokens.issue(UUID.randomUUID(), tenantId, "ADMINISTRATOR", ADMIN_EMAIL);
    String tripId =
        api.schedule(token, "Lima", "Arequipa", "Repuestos", null, LocalDate.now().plusDays(5), false)
            .andReturn()
            .getResponse()
            .getContentAsString();
    String id = objectMapper.readTree(tripId).get("id").asText();
    fleetStub.setUnavailable(true);

    api.assign(token, id, UUID.randomUUID().toString(), UUID.randomUUID().toString(), false)
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.detail").value(FLEET_DOWN_MESSAGE));

    api.detail(token, id)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SCHEDULED"));
  }

  @Test
  void schedulingDoesNotDependOnFleet() throws Exception {
    String token = tokens.issue(UUID.randomUUID(), UUID.randomUUID(), "ADMINISTRATOR", ADMIN_EMAIL);
    fleetStub.setUnavailable(true);

    api.schedule(token, "Lima", "Cusco", "Muebles", null, LocalDate.now().plusDays(5), false)
        .andExpect(status().isCreated());
  }
}
