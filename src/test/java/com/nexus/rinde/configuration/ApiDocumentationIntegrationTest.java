package com.nexus.rinde.configuration;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexus.rinde.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class ApiDocumentationIntegrationTest extends AbstractIntegrationTest {

  @Test
  void healthCheckIsPublic() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void openApiDocumentListsTheEndpointsWithBearerSecurity() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paths", hasKey("/api/v1/tenants")))
        .andExpect(jsonPath("$.paths", hasKey("/api/v1/tenants/{id}/verification")))
        .andExpect(jsonPath("$.paths", hasKey("/api/v1/auth/sign-in")))
        .andExpect(jsonPath("$.paths", hasKey("/api/v1/auth/password-reset")))
        .andExpect(jsonPath("$.paths", hasKey("/api/v1/auth/password")))
        .andExpect(jsonPath("$.paths", hasKey("/api/v1/users")))
        .andExpect(jsonPath("$.paths", hasKey("/api/v1/users/{id}")))
        .andExpect(jsonPath("$.paths", hasKey("/api/v1/trips")))
        .andExpect(jsonPath("$.paths", hasKey("/api/v1/trips/{id}/assignment")))
        .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
  }

  @Test
  void swaggerUiIsReachableWithoutToken() throws Exception {
    mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
  }
}
