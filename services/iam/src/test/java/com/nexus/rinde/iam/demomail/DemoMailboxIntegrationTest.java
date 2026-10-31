package com.nexus.rinde.iam.demomail;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.nexus.rinde.iam.demomail.application.DemoMailCapture;
import com.nexus.rinde.iam.domain.model.commands.RegisterTenantCommand;
import com.nexus.rinde.iam.domain.model.events.TenantRegistered;
import com.nexus.rinde.iam.domain.services.TenantCommandService;
import com.nexus.rinde.shared.infrastructure.security.JwtClaims;
import com.nexus.rinde.support.AbstractIntegrationTest;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.transaction.support.TransactionTemplate;

class DemoMailboxIntegrationTest extends AbstractIntegrationTest {
  private static final String OWNER_KEY = "demo-mailbox-owner-test-key-distinct-from-company";
  private static final String PASSWORD = "MailboxTestPassword!42";
  @Autowired JdbcTemplate jdbc;
  @Autowired DemoMailCapture capture;
  @Autowired TenantCommandService tenants;
  @Autowired org.springframework.transaction.PlatformTransactionManager transactions;

  @DynamicPropertySource
  static void demo(DynamicPropertyRegistry registry) {
    registry.add("rinde.demo.mailbox.enabled", () -> true);
    registry.add("rinde.demo.mailbox.encryption-key", () -> "a".repeat(64));
    registry.add("rinde.platform.jwt.secret", () -> OWNER_KEY);
    registry.add(
        "rinde.security.jwt.secret", () -> "demo-mailbox-company-test-key-distinct-from-owner");
    registry.add("rinde.platform.bootstrap.enabled", () -> false);
  }

  private String owner() {
    return Jwts.builder()
        .subject(UUID.randomUUID().toString())
        .issuer("rinde-platform")
        .audience()
        .add("rinde-platform-api")
        .and()
        .claim("scope", "PLATFORM")
        .claim("role", "PLATFORM_OWNER")
        .claim("email", "operator@example.invalid")
        .expiration(Date.from(Instant.now().plusSeconds(300)))
        .signWith(JwtClaims.signingKey(OWNER_KEY))
        .compact();
  }

  private com.fasterxml.jackson.databind.JsonNode list(String recipient, String purpose)
      throws Exception {
    String body =
        mockMvc
            .perform(
                get("/api/v1/platform/demo-mailbox/messages")
                    .param("search", recipient)
                    .param("purpose", purpose)
                    .header("Authorization", "Bearer " + owner()))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-store"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(body);
  }

  private com.fasterxml.jackson.databind.JsonNode open(String id) throws Exception {
    return objectMapper.readTree(
        mockMvc
            .perform(
                get("/api/v1/platform/demo-mailbox/messages/" + id)
                    .header("Authorization", "Bearer " + owner()))
            .andExpect(status().isOk())
            .andExpect(header().string("Referrer-Policy", "no-referrer"))
            .andReturn()
            .getResponse()
            .getContentAsString());
  }

  private String token(com.fasterxml.jackson.databind.JsonNode message) {
    return org.springframework.web.util.UriComponentsBuilder.fromUriString(
            message.get("actionPath").asText())
        .build()
        .getQueryParams()
        .getFirst("token");
  }

  @Test
  void registrationCapturesOnlyCommittedMailAndVerifiesWithItsRealLink() throws Exception {
    String id =
        api.json(
                api.registerTenant(
                    "Empresa demo", "20123456789", "Ana", "ana@example.invalid", PASSWORD))
            .get("id")
            .asText();
    var event = capturedEvents.last(TenantRegistered.class).orElseThrow();
    var page = list("ana@example.invalid", "EMAIL_VERIFICATION");
    assertThat(page.get("total").asInt()).isEqualTo(1);
    assertThat(page.toString())
        .doesNotContain("actionPath", event.verificationToken(), "encryptedToken", "tokenHash");
    String encrypted =
        jdbc.queryForObject("select encrypted_token from iam.demo_mail_messages", String.class);
    assertThat(encrypted).doesNotContain(event.verificationToken());
    var mail = open(page.get("items").get(0).get("id").asText());
    assertThat(mail.get("message").get("state").asText()).isEqualTo("ACTIVE");
    assertThat(mail.get("message").get("readAt").isNull()).isFalse();
    api.verifyTenant(id, token(mail)).andExpect(status().isOk());
    var consumed = open(mail.get("message").get("id").asText());
    assertThat(consumed.get("message").get("state").asText()).isEqualTo("USED");
    assertThat(consumed.get("actionPath").isNull()).isTrue();
    api.verifyTenant(id, token(mail)).andExpect(status().isBadRequest());
  }

  @Test
  void invitedUserSetsPasswordAndCanSignInUsingTheMailboxLink() throws Exception {
    api.registerAndVerifyTenant("20123456789", "admin@example.invalid", PASSWORD);
    String administrator = api.signInForToken("admin@example.invalid", PASSWORD);
    api.inviteUser(administrator, "Conductor", "driver@example.invalid", "DRIVER")
        .andExpect(status().isCreated());
    var page = list("driver@example.invalid", "INVITATION");
    var mail = open(page.get("items").get(0).get("id").asText());
    assertThat(mail.get("actionPath").asText()).startsWith("/accept-invitation?token=");
    api.definePassword(token(mail), PASSWORD).andExpect(status().isNoContent());
    assertThat(api.signInForToken("driver@example.invalid", PASSWORD)).isNotBlank();
    api.definePassword(token(mail), PASSWORD).andExpect(status().isBadRequest());
  }

  @Test
  void recoveryUsesAnActualOneTimeLinkWithoutEnumeratingUnknownAccounts() throws Exception {
    api.registerAndVerifyTenant("20123456789", "ana@example.invalid", PASSWORD);
    api.requestPasswordReset("ana@example.invalid").andExpect(status().isAccepted());
    var mail =
        open(list("ana@example.invalid", "PASSWORD_RESET").get("items").get(0).get("id").asText());
    api.definePassword(token(mail), "ChangedMailboxPassword!42").andExpect(status().isNoContent());
    api.signIn("ana@example.invalid", PASSWORD).andExpect(status().isUnauthorized());
    assertThat(api.signInForToken("ana@example.invalid", "ChangedMailboxPassword!42")).isNotBlank();
    api.requestPasswordReset("unknown@example.invalid").andExpect(status().isAccepted());
    assertThat(list("unknown@example.invalid", "PASSWORD_RESET").get("total").asInt()).isZero();
  }

  @Test
  void expiredOrDisabledAccountsDoNotExposeAnAction() throws Exception {
    api.registerTenant("Empresa demo", "20123456789", "Ana", "ana@example.invalid", PASSWORD)
        .andExpect(status().isCreated());
    String id =
        list("ana@example.invalid", "EMAIL_VERIFICATION").get("items").get(0).get("id").asText();
    jdbc.update(
        "update iam.demo_mail_messages set expires_at=? where id=?",
        java.sql.Timestamp.from(Instant.now().minusSeconds(60)),
        UUID.fromString(id));
    var expired = open(id);
    assertThat(expired.get("message").get("state").asText()).isEqualTo("EXPIRED");
    assertThat(expired.get("actionPath").isNull()).isTrue();
    jdbc.update("update iam.users set status='DISABLED'");
    var disabled = open(id);
    assertThat(disabled.get("message").get("state").asText()).isEqualTo("INVALID");
    assertThat(disabled.get("actionPath").isNull()).isTrue();
  }

  @Test
  void twoConcurrentSubmissionsCannotConsumeTheSameRecoveryLinkTwice() throws Exception {
    api.registerAndVerifyTenant("20123456789", "ana@example.invalid", PASSWORD);
    api.requestPasswordReset("ana@example.invalid").andExpect(status().isAccepted());
    var message =
        open(list("ana@example.invalid", "PASSWORD_RESET").get("items").get(0).get("id").asText());
    String raw = token(message);
    var gate = new java.util.concurrent.CountDownLatch(1);
    var pool = java.util.concurrent.Executors.newFixedThreadPool(2);
    try {
      java.util.concurrent.Callable<Integer> attempt =
          () -> {
            gate.await();
            return api.definePassword(raw, "ConcurrentMailboxPassword!42")
                .andReturn()
                .getResponse()
                .getStatus();
          };
      var first = pool.submit(attempt);
      var second = pool.submit(attempt);
      gate.countDown();
      assertThat(
              List.of(
                  first.get(15, java.util.concurrent.TimeUnit.SECONDS),
                  second.get(15, java.util.concurrent.TimeUnit.SECONDS)))
          .containsExactlyInAnyOrder(204, 400);
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void companiesAndAnonymousVisitorsCannotReadAnyMailbox() throws Exception {
    api.registerAndVerifyTenant("20123456789", "admin@example.invalid", PASSWORD);
    String administrator = api.signInForToken("admin@example.invalid", PASSWORD);
    mockMvc
        .perform(get("/api/v1/platform/demo-mailbox/messages"))
        .andExpect(status().isUnauthorized());
    mockMvc
        .perform(
            get("/api/v1/platform/demo-mailbox/messages")
                .header("Authorization", "Bearer " + administrator))
        .andExpect(status().is4xxClientError());
    mockMvc
        .perform(
            get("/api/v1/platform/demo-mailbox/messages/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + administrator))
        .andExpect(status().is4xxClientError());
    mockMvc
        .perform(get("/api/v1/demo/features"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mailboxEnabled").value(true));
    mockMvc
        .perform(
            get("/api/v1/platform/demo-mailbox/messages/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + owner()))
        .andExpect(status().isNotFound());
  }

  @Test
  void duplicateEventsProduceOneMessageAndRollbackProducesNone() throws Exception {
    api.registerTenant("Empresa demo", "20123456789", "Ana", "ana@example.invalid", PASSWORD)
        .andExpect(status().isCreated());
    var event = capturedEvents.last(TenantRegistered.class).orElseThrow();
    new TransactionTemplate(transactions)
        .executeWithoutResult(
            status -> {
              capture.registered(event);
              capture.registered(event);
            });
    assertThat(jdbc.queryForObject("select count(*) from iam.demo_mail_messages", Long.class))
        .isEqualTo(1);
    new TransactionTemplate(transactions)
        .executeWithoutResult(
            status -> {
              tenants.handle(
                  new RegisterTenantCommand(
                      "Empresa revertida",
                      "20987654321",
                      "Luis",
                      "rollback@example.invalid",
                      PASSWORD));
              status.setRollbackOnly();
            });
    assertThat(list("rollback@example.invalid", "EMAIL_VERIFICATION").get("total").asInt())
        .isZero();
    assertThat(jdbc.queryForObject("select count(*) from iam.demo_mail_messages", Long.class))
        .isEqualTo(1);
  }
}
