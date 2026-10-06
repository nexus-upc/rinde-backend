package com.nexus.rinde.acceptance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.rinde.iam.domain.model.events.TenantRegistered;
import com.nexus.rinde.subscription.interfaces.acl.SubscriptionActivated;
import com.nexus.rinde.subscription.interfaces.acl.SubscriptionSuspended;
import com.nexus.rinde.support.CapturedEvents;
import com.nexus.rinde.support.IamApi;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.web.servlet.MockMvc;

/** Pasos de la empresa: registro, verificación del correo y cambios de suscripción (US08). */
public class TenantSteps {

  private final ScenarioContext context;
  private final CapturedEvents capturedEvents;
  private final ApplicationEventPublisher publisher;
  private final IamApi api;

  public TenantSteps(
      ScenarioContext context,
      CapturedEvents capturedEvents,
      ApplicationEventPublisher publisher,
      MockMvc mockMvc,
      ObjectMapper objectMapper) {
    this.context = context;
    this.capturedEvents = capturedEvents;
    this.publisher = publisher;
    this.api = new IamApi(mockMvc, objectMapper, capturedEvents);
  }

  @Dado("que existe una empresa verificada con RUC {string} y administrador {string}")
  public void aVerifiedTenantExists(String ruc, String administratorEmail) throws Exception {
    String tenantId =
        api.registerAndVerifyTenant(ruc, administratorEmail, ScenarioContext.PASSWORD);
    context.rememberTenantId(ruc, tenantId);
  }

  @Dado("que registré la empresa con RUC {string} y administrador {string} sin confirmar el correo")
  public void iRegisteredATenantWithoutVerifying(String ruc, String administratorEmail)
      throws Exception {
    registerTenant("Empresa " + ruc, ruc, administratorEmail);
  }

  @Cuando("registro la empresa {string} con RUC {string} y administrador {string}")
  public void iRegisterTheTenant(String tradeName, String ruc, String administratorEmail)
      throws Exception {
    registerTenant(tradeName, ruc, administratorEmail);
  }

  @Cuando("confirmo el correo de la empresa con RUC {string} usando el enlace recibido")
  public void iVerifyTheTenantWithTheReceivedLink(String ruc) throws Exception {
    String token = capturedEvents.last(TenantRegistered.class).orElseThrow().verificationToken();
    context.record(api.verifyTenant(context.tenantIdOf(ruc), token));
  }

  @Cuando("confirmo el correo de la empresa con RUC {string} usando el enlace {string}")
  public void iVerifyTheTenantWithTheLink(String ruc, String token) throws Exception {
    context.record(api.verifyTenant(context.tenantIdOf(ruc), token));
  }

  @Cuando("confirmo el correo de una empresa que no existe usando el enlace {string}")
  public void iVerifyAnUnknownTenant(String token) throws Exception {
    context.record(api.verifyTenant(UUID.randomUUID().toString(), token));
  }

  @Dado("que la suscripción de la empresa con RUC {string} fue suspendida")
  public void theSubscriptionWasSuspended(String ruc) {
    publisher.publishEvent(
        new SubscriptionSuspended(
            UUID.randomUUID(), Instant.now(), UUID.fromString(context.tenantIdOf(ruc))));
  }

  @Dado("que la suscripción de la empresa con RUC {string} fue activada")
  public void theSubscriptionWasActivated(String ruc) {
    publisher.publishEvent(
        new SubscriptionActivated(
            UUID.randomUUID(), Instant.now(), UUID.fromString(context.tenantIdOf(ruc))));
  }

  private void registerTenant(String tradeName, String ruc, String administratorEmail)
      throws Exception {
    context.record(
        api.registerTenant(
            tradeName,
            ruc,
            "Administrador de " + tradeName,
            administratorEmail,
            ScenarioContext.PASSWORD));
    if (context.statusCode() == 201) {
      context.rememberTenantId(ruc, context.body().get("id").asText());
    }
  }
}
