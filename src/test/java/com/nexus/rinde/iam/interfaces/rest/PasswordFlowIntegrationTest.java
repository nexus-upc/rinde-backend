package com.nexus.rinde.iam.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexus.rinde.iam.domain.model.events.PasswordResetRequested;
import com.nexus.rinde.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class PasswordFlowIntegrationTest extends AbstractIntegrationTest {

  private static final String PASSWORD = "Clave-Segura-2026";
  private static final String NEW_PASSWORD = "Nueva-Clave-2026";

  @Autowired private JdbcTemplate jdbcTemplate;

  private String requestResetToken() throws Exception {
    api.registerAndVerifyTenant("20123456789", "ana@andes.pe", PASSWORD);
    api.requestPasswordReset("ana@andes.pe").andExpect(status().isAccepted());
    return capturedEvents.last(PasswordResetRequested.class).orElseThrow().resetToken();
  }

  @Test
  void passwordResetAlwaysRespondsAcceptedEvenForUnknownEmails() throws Exception {
    api.requestPasswordReset("nadie@andes.pe").andExpect(status().isAccepted());

    assertThat(capturedEvents.last(PasswordResetRequested.class)).isEmpty();
  }

  @Test
  void passwordResetForARegisteredEmailPublishesTheEvent() throws Exception {
    api.registerAndVerifyTenant("20123456789", "ana@andes.pe", PASSWORD);

    api.requestPasswordReset("ANA@andes.pe").andExpect(status().isAccepted());

    assertThat(capturedEvents.last(PasswordResetRequested.class)).isPresent();
  }

  @Test
  void theLinkChangesThePasswordAndKeepsTheUserActive() throws Exception {
    String token = requestResetToken();

    api.definePassword(token, NEW_PASSWORD).andExpect(status().isNoContent());

    api.signIn("ana@andes.pe", NEW_PASSWORD).andExpect(status().isOk());
    api.signIn("ana@andes.pe", PASSWORD).andExpect(status().isUnauthorized());
  }

  @Test
  void aUsedLinkReturns400() throws Exception {
    String token = requestResetToken();
    api.definePassword(token, NEW_PASSWORD).andExpect(status().isNoContent());

    api.definePassword(token, "Otra-Clave-2026")
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail").value("El enlace no es válido, venció o ya fue utilizado."));
  }

  @Test
  void anExpiredLinkReturns400() throws Exception {
    String token = requestResetToken();
    jdbcTemplate.update(
        "update iam.access_tokens set expires_at = (now() at time zone 'utc') - interval '1 minute'");

    api.definePassword(token, NEW_PASSWORD)
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail").value("El enlace no es válido, venció o ya fue utilizado."));
    api.signIn("ana@andes.pe", PASSWORD).andExpect(status().isOk());
  }

  @Test
  void anUnknownLinkReturns400() throws Exception {
    api.definePassword("enlace-inventado", NEW_PASSWORD).andExpect(status().isBadRequest());
  }

  @Test
  void aShortPasswordReturns400() throws Exception {
    String token = requestResetToken();

    api.definePassword(token, "corta")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.password").exists());
    api.definePassword(token, NEW_PASSWORD).andExpect(status().isNoContent());
  }
}
