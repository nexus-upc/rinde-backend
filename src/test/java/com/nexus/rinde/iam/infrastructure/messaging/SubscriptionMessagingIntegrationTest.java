package com.nexus.rinde.iam.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.nexus.rinde.iam.application.events.SubscriptionStatusMessage;
import com.nexus.rinde.iam.domain.model.commands.RestrictTenantCommand;
import com.nexus.rinde.iam.domain.services.TenantCommandService;
import com.nexus.rinde.support.AbstractIntegrationTest;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/** Comprueba cambios reales, duplicados y mensajes desordenados entre RabbitMQ y la base de IAM. */
class SubscriptionMessagingIntegrationTest extends AbstractIntegrationTest {

  @Autowired private RabbitTemplate rabbit;
  @Autowired private TopicExchange eventsExchange;
  @Autowired private AmqpAdmin admin;
  @Autowired private RabbitListenerEndpointRegistry listeners;
  @Autowired private Environment environment;
  @Autowired private JdbcTemplate jdbc;
  @MockitoSpyBean private TenantCommandService commands;

  @BeforeEach
  void startConsumer() {
    admin.initialize();
    admin.purgeQueue(queue(), false);
    admin.purgeQueue(queue() + ".dead", false);
    listeners.start();
  }

  @AfterEach
  void stopConsumer() {
    listeners.stop();
  }

  @AfterAll
  static void removeQueues(@Autowired AmqpAdmin admin, @Autowired Environment environment) {
    String name = environment.getRequiredProperty("rinde.messaging.subscription.queue");
    admin.deleteQueue(name);
    admin.deleteQueue(name + ".dead");
  }

  @Test
  void suspendsAndReactivatesOnlyTheTargetTenant() throws Exception {
    UUID tenant = tenant("20123456789", "ana@andes.pe");
    UUID other = tenant("20987654321", "otro@andes.pe");
    SubscriptionStatusMessage suspended = event(tenant, Instant.now());
    send("subscription.suspended", suspended);
    awaitReceipt(suspended.eventId());
    assertThat(status(tenant)).isEqualTo("RESTRICTED");
    assertThat(status(other)).isEqualTo("ACTIVE");

    SubscriptionStatusMessage activated = event(tenant, suspended.occurredAt().plusSeconds(1));
    send("subscription.activated", activated);
    awaitReceipt(activated.eventId());
    assertThat(status(tenant)).isEqualTo("ACTIVE");
  }

  @Test
  void ignoresTheSameSuspensionAfterALaterReactivation() throws Exception {
    UUID tenant = tenant("20123456789", "ana@andes.pe");
    SubscriptionStatusMessage suspended = event(tenant, Instant.now());
    SubscriptionStatusMessage activated = event(tenant, suspended.occurredAt().plusSeconds(1));
    send("subscription.suspended", suspended);
    send("subscription.activated", activated);
    send("subscription.suspended", suspended);
    SubscriptionStatusMessage sentinel = event(tenant, activated.occurredAt().plusSeconds(1));
    send("subscription.activated", sentinel);
    awaitReceipt(sentinel.eventId());

    assertThat(status(tenant)).isEqualTo("ACTIVE");
    assertThat(receiptCount()).isEqualTo(3);
  }

  @Test
  void ignoresADelayedSuspensionOlderThanTheLastActivation() throws Exception {
    UUID tenant = tenant("20123456789", "ana@andes.pe");
    Instant now = Instant.now();
    SubscriptionStatusMessage activated = event(tenant, now);
    SubscriptionStatusMessage delayed = event(tenant, now.minusSeconds(10));
    send("subscription.activated", activated);
    send("subscription.suspended", delayed);
    awaitReceipt(delayed.eventId());

    assertThat(status(tenant)).isEqualTo("ACTIVE");
    assertThat(receiptCount()).isEqualTo(2);
  }

  @Test
  void preservesAnUnknownTenantEventForInspectionInsteadOfAcknowledgingIt() {
    SubscriptionStatusMessage event = event(UUID.randomUUID(), Instant.now());
    send("subscription.suspended", event);
    Message rejected = rabbit.receive(queue() + ".dead", 10_000);

    assertThat(rejected).isNotNull();
    assertThat(receiptCount()).isZero();
  }

  @Test
  void rollsBackTheReceiptWhenApplyingTheStateFails() throws Exception {
    UUID tenant = tenant("20123456789", "ana@andes.pe");
    doThrow(new DataAccessResourceFailureException("Fallo temporal al guardar."))
        .when(commands).handle(any(RestrictTenantCommand.class));
    send("subscription.suspended", event(tenant, Instant.now()));

    assertThat(rabbit.receive(queue() + ".dead", 10_000)).isNotNull();
    assertThat(status(tenant)).isEqualTo("ACTIVE");
    assertThat(receiptCount()).isZero();
    verify(commands, times(3)).handle(any(RestrictTenantCommand.class));
  }

  @Test
  void rejectsMissingIdentifiersWithoutChangingTenantStatus() throws Exception {
    UUID tenant = tenant("20123456789", "ana@andes.pe");
    MessageProperties properties = new MessageProperties();
    properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
    rabbit.send(eventsExchange.getName(), "subscription.suspended",
        new Message("{}".getBytes(java.nio.charset.StandardCharsets.UTF_8), properties));

    assertThat(rabbit.receive(queue() + ".dead", 10_000)).isNotNull();
    assertThat(status(tenant)).isEqualTo("ACTIVE");
    assertThat(receiptCount()).isZero();
  }

  private UUID tenant(String ruc, String email) throws Exception {
    return UUID.fromString(api.registerAndVerifyTenant(ruc, email, "Clave-Segura-2026"));
  }

  private SubscriptionStatusMessage event(UUID tenant, Instant time) {
    return new SubscriptionStatusMessage(UUID.randomUUID(), time, tenant);
  }

  private void send(String routingKey, SubscriptionStatusMessage event) {
    rabbit.convertAndSend(eventsExchange.getName(), routingKey, event,
        message -> {
          message.getMessageProperties().setHeader("__TypeId__",
              "com.nexus.rinde.subscription.interfaces.acl.SubscriptionActivated");
          return message;
        });
  }

  private String queue() {
    return environment.getRequiredProperty("rinde.messaging.subscription.queue");
  }

  private void awaitReceipt(UUID id) {
    await().atMost(Duration.ofSeconds(10)).until(() ->
        jdbc.queryForObject("select count(*) from iam.subscription_event_receipts where event_id = ?",
            Integer.class, id) == 1);
  }

  private String status(UUID tenant) {
    return jdbc.queryForObject("select status from iam.tenants where id = ?", String.class, tenant);
  }

  private int receiptCount() {
    return jdbc.queryForObject("select count(*) from iam.subscription_event_receipts", Integer.class);
  }
}
