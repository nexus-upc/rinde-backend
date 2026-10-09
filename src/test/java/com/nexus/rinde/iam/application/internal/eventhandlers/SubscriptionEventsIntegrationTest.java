package com.nexus.rinde.iam.application.internal.eventhandlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexus.rinde.shared.infrastructure.security.JwtTokenReader;
import com.nexus.rinde.subscription.interfaces.acl.SubscriptionActivated;
import com.nexus.rinde.subscription.interfaces.acl.SubscriptionSuspended;
import com.nexus.rinde.support.AbstractIntegrationTest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;

/** Simula con eventos in-process lo que publicará Subscriptions & Billing. */
class SubscriptionEventsIntegrationTest extends AbstractIntegrationTest {

  private static final String PASSWORD = "Clave-Segura-2026";

  @Autowired private ApplicationEventPublisher publisher;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private JwtTokenReader tokenReader;

  private String tenantStatus(String tenantId) {
    return jdbcTemplate.queryForObject(
        "select status from iam.tenants where id = ?", String.class, UUID.fromString(tenantId));
  }

  private void suspend(String tenantId) {
    publisher.publishEvent(
        new SubscriptionSuspended(UUID.randomUUID(), Instant.now(), UUID.fromString(tenantId)));
  }

  private void activate(String tenantId) {
    publisher.publishEvent(
        new SubscriptionActivated(UUID.randomUUID(), Instant.now(), UUID.fromString(tenantId)));
  }

  @Test
  void suspendedSubscriptionRestrictsTheTenantAndTheTokenCarriesTheStatus() throws Exception {
    String tenantId = api.registerAndVerifyTenant("20123456789", "ana@andes.pe", PASSWORD);

    suspend(tenantId);

    assertThat(tenantStatus(tenantId)).isEqualTo("RESTRICTED");
    api.signIn("ana@andes.pe", PASSWORD).andExpect(status().isOk());
    String token = api.signInForToken("ana@andes.pe", PASSWORD);
    assertThat(tokenReader.read(token).orElseThrow().tenantStatus()).isEqualTo("RESTRICTED");
  }

  @Test
  void activatedSubscriptionReactivatesTheTenant() throws Exception {
    String tenantId = api.registerAndVerifyTenant("20123456789", "ana@andes.pe", PASSWORD);
    suspend(tenantId);

    activate(tenantId);

    assertThat(tenantStatus(tenantId)).isEqualTo("ACTIVE");
  }

  @Test
  void repeatedEventsDoNotChangeTheResult() throws Exception {
    String tenantId = api.registerAndVerifyTenant("20123456789", "ana@andes.pe", PASSWORD);

    suspend(tenantId);
    suspend(tenantId);
    assertThat(tenantStatus(tenantId)).isEqualTo("RESTRICTED");

    activate(tenantId);
    activate(tenantId);
    assertThat(tenantStatus(tenantId)).isEqualTo("ACTIVE");
  }

  @Test
  void aTenantPendingVerificationIgnoresSubscriptionEvents() throws Exception {
    String body =
        api.registerTenant("Transportes Andes", "20123456789", "Ana", "ana@andes.pe", PASSWORD)
            .andReturn()
            .getResponse()
            .getContentAsString();
    String tenantId = objectMapper.readTree(body).get("id").asText();

    suspend(tenantId);
    activate(tenantId);

    assertThat(tenantStatus(tenantId)).isEqualTo("PENDING_VERIFICATION");
  }

  @Test
  void anUnknownTenantDoesNotBreakThePublisher() {
    assertThatCode(() -> suspend(UUID.randomUUID().toString())).doesNotThrowAnyException();
    assertThatCode(() -> activate(UUID.randomUUID().toString())).doesNotThrowAnyException();
  }
}
