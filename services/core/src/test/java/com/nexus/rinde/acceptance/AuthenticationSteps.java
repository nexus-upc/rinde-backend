package com.nexus.rinde.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.rinde.iam.domain.model.events.PasswordResetRequested;
import com.nexus.rinde.shared.infrastructure.security.CurrentUser;
import com.nexus.rinde.shared.infrastructure.security.JwtTokenReader;
import com.nexus.rinde.support.CapturedEvents;
import com.nexus.rinde.support.IamApi;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/** Pasos de acceso: inicio de sesión (US09) y recuperación o definición de contraseña (US10). */
public class AuthenticationSteps {

  private final ScenarioContext context;
  private final CapturedEvents capturedEvents;
  private final JwtTokenReader tokenReader;
  private final JdbcTemplate jdbcTemplate;
  private final IamApi api;

  public AuthenticationSteps(
      ScenarioContext context,
      CapturedEvents capturedEvents,
      JwtTokenReader tokenReader,
      JdbcTemplate jdbcTemplate,
      MockMvc mockMvc,
      ObjectMapper objectMapper) {
    this.context = context;
    this.capturedEvents = capturedEvents;
    this.tokenReader = tokenReader;
    this.jdbcTemplate = jdbcTemplate;
    this.api = new IamApi(mockMvc, objectMapper, capturedEvents);
  }

  @Cuando("{string} inicia sesión con la contraseña {string}")
  public void signIn(String email, String password) throws Exception {
    context.record(api.signIn(email, password));
    if (context.statusCode() == 200) {
      context.rememberAccessToken(email, context.body().get("accessToken").asText());
    }
  }

  @Y(
      "el token de acceso indica el rol {string}, la empresa de RUC {string} y el estado de empresa {string}")
  public void theTokenCarries(String role, String ruc, String tenantStatus) throws Exception {
    String token = context.body().get("accessToken").asText();
    CurrentUser user = tokenReader.read(token).orElseThrow();
    assertThat(user.role()).isEqualTo(role);
    assertThat(user.tenantId().toString()).isEqualTo(context.tenantIdOf(ruc));
    assertThat(user.tenantStatus()).isEqualTo(tenantStatus);
  }

  @Cuando("{string} solicita recuperar su contraseña")
  public void requestPasswordReset(String email) throws Exception {
    context.record(api.requestPasswordReset(email));
  }

  @Cuando("defino la contraseña {string} con el enlace de recuperación recibido")
  public void definePasswordWithTheResetLink(String password) throws Exception {
    context.record(api.definePassword(lastResetToken(), password));
  }

  @Cuando("defino la contraseña {string} con el enlace {string}")
  public void definePasswordWithTheLink(String password, String token) throws Exception {
    context.record(api.definePassword(token, password));
  }

  @Dado("que cambié mi contraseña a {string} con el enlace de recuperación recibido")
  public void iAlreadyChangedMyPassword(String password) throws Exception {
    context
        .record(api.definePassword(lastResetToken(), password))
        .andExpect(status().isNoContent());
  }

  @Dado("que el enlace recibido venció")
  public void theReceivedLinkExpired() {
    jdbcTemplate.update(
        "update iam.access_tokens set expires_at = (now() at time zone 'utc') - interval '1 minute'");
  }

  @Entonces("{string} puede iniciar sesión con la contraseña {string}")
  public void canSignIn(String email, String password) throws Exception {
    api.signIn(email, password).andExpect(status().isOk());
  }

  @Entonces("{string} no puede iniciar sesión con la contraseña {string}")
  public void cannotSignIn(String email, String password) throws Exception {
    api.signIn(email, password).andExpect(status().isUnauthorized());
  }

  private String lastResetToken() {
    return capturedEvents.last(PasswordResetRequested.class).orElseThrow().resetToken();
  }
}
