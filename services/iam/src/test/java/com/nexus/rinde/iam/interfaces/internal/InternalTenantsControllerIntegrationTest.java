package com.nexus.rinde.iam.interfaces.internal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexus.rinde.support.AbstractIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Consultas internas que usan los demás servicios: estado, usuario, rol y correo del admin. */
class InternalTenantsControllerIntegrationTest extends AbstractIntegrationTest {

  private static final String PASSWORD = "Clave-Segura-2026";
  private static final String RUC = "20123456789";
  private static final String ADMIN = "ana@andes.pe";

  @Test
  void returnsTheCurrentStatusOfTheTenant() throws Exception {
    String tenantId = api.registerAndVerifyTenant(RUC, ADMIN, PASSWORD);

    mockMvc
        .perform(get("/api/internal/v1/tenants/{tenantId}/status", tenantId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tenantId").value(tenantId))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  void answersNotFoundForAnUnknownTenant() throws Exception {
    mockMvc
        .perform(get("/api/internal/v1/tenants/{tenantId}/status", UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("No existe la empresa indicada."));
  }

  @Test
  void returnsTheUserOfTheTenant() throws Exception {
    String tenantId = api.registerAndVerifyTenant(RUC, ADMIN, PASSWORD);
    String userId = adminId();

    mockMvc
        .perform(get("/api/internal/v1/tenants/{tenantId}/users/{userId}", tenantId, userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(userId))
        .andExpect(jsonPath("$.tenantId").value(tenantId))
        .andExpect(jsonPath("$.email").value(ADMIN))
        .andExpect(jsonPath("$.role").value("ADMINISTRATOR"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  void answersNotFoundForAUserOfAnotherTenant() throws Exception {
    api.registerAndVerifyTenant(RUC, ADMIN, PASSWORD);
    String otherTenantId = api.registerAndVerifyTenant("20987654321", "otro@otra.pe", PASSWORD);

    mockMvc
        .perform(
            get(
                "/api/internal/v1/tenants/{tenantId}/users/{userId}",
                otherTenantId,
                adminId()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("No existe el usuario en la empresa."));
  }

  @Test
  void tellsWhetherTheUserIsActiveWithTheRole() throws Exception {
    String tenantId = api.registerAndVerifyTenant(RUC, ADMIN, PASSWORD);
    String userId = adminId();

    mockMvc
        .perform(
            get(
                "/api/internal/v1/tenants/{tenantId}/users/{userId}/active-role",
                tenantId,
                userId)
                .param("role", "ADMINISTRATOR"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(true));

    mockMvc
        .perform(
            get(
                "/api/internal/v1/tenants/{tenantId}/users/{userId}/active-role",
                tenantId,
                userId)
                .param("role", "DRIVER"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));
  }

  @Test
  void returnsTheAdministratorEmailOfTheTenant() throws Exception {
    String tenantId = api.registerAndVerifyTenant(RUC, ADMIN, PASSWORD);

    mockMvc
        .perform(get("/api/internal/v1/tenants/{tenantId}/administrator-email", tenantId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(ADMIN));
  }

  @Test
  void documentsTheInternalEndpointsWithTheInternalTag() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.paths['/api/internal/v1/tenants/{tenantId}/status'].get.tags[0]")
                .value("Internal"));
  }

  private String adminId() throws Exception {
    String token = api.signInForToken(ADMIN, PASSWORD);
    return api.json(api.listUsers(token)).get(0).get("id").asText();
  }
}
