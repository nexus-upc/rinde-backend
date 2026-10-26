package com.nexus.rinde.iam.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexus.rinde.iam.domain.model.events.TenantRegistered;
import com.nexus.rinde.shared.infrastructure.security.CurrentUser;
import com.nexus.rinde.shared.infrastructure.security.JwtTokenReader;
import com.nexus.rinde.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class AuthenticationControllerIntegrationTest extends AbstractIntegrationTest {

  private static final String PASSWORD = "Clave-Segura-2026";

  @Autowired private JwtTokenReader tokenReader;

  @Test
  void signInReturnsATokenWithUserRoleTenantAndTenantStatus() throws Exception {
    String tenantId = api.registerAndVerifyTenant("20123456789", "ana@andes.pe", PASSWORD);

    String token =
        api.json(
                api.signIn("ana@andes.pe", PASSWORD)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresInSeconds").value(8 * 3600)))
            .get("accessToken")
            .asText();

    CurrentUser current = tokenReader.read(token).orElseThrow();
    assertThat(current.tenantId().toString()).isEqualTo(tenantId);
    assertThat(current.role()).isEqualTo("ADMINISTRATOR");
    assertThat(current.tenantStatus()).isEqualTo("ACTIVE");
    assertThat(current.email()).isEqualTo("ana@andes.pe");
  }

  @Test
  void signInWithAWrongPasswordReturns401() throws Exception {
    api.registerAndVerifyTenant("20123456789", "ana@andes.pe", PASSWORD);

    api.signIn("ana@andes.pe", "otra-clave-incorrecta")
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("El correo o la contraseña son incorrectos."))
        .andExpect(jsonPath("$.traceId").exists());
  }

  @Test
  void signInWithAnUnknownEmailReturnsTheSame401() throws Exception {
    api.signIn("nadie@andes.pe", PASSWORD)
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("El correo o la contraseña son incorrectos."));
  }

  @Test
  void signInIsForbiddenWhileTheTenantIsPendingVerification() throws Exception {
    api.registerTenant("Transportes Andes", "20123456789", "Ana", "ana@andes.pe", PASSWORD)
        .andExpect(status().isCreated());
    assertThat(capturedEvents.last(TenantRegistered.class)).isPresent();

    api.signIn("ana@andes.pe", PASSWORD)
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.detail").value(containsString("confirmó su correo")));
  }

  @Test
  void signInWithoutBodyFieldsReturns400() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/sign-in").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.email").exists());
  }

  @Test
  void protectedRoutesRejectRequestsWithoutAToken() throws Exception {
    mockMvc
        .perform(get("/api/v1/users"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.traceId").exists());
  }
}
