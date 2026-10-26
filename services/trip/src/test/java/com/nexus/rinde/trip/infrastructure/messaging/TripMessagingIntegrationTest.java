package com.nexus.rinde.trip.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexus.rinde.support.AbstractIntegrationTest;
import com.nexus.rinde.support.Awaits;
import com.nexus.rinde.trip.application.internal.eventhandlers.SettlementClosed;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Pruebas contra el broker RabbitMQ local: el evento TripAssigned sale con su routing key y su
 * eventId, y un settlement.closed duplicado se procesa una sola vez.
 */
class TripMessagingIntegrationTest extends AbstractIntegrationTest {

  private static final String ADMIN_EMAIL = "ana@andes.pe";
  private static final String DRIVER_EMAIL = "luis@andes.pe";

  @Autowired private RabbitTemplate rabbitTemplate;
  @Autowired private AmqpAdmin amqpAdmin;
  @Autowired private TopicExchange rindeEventsExchange;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void publishesTripAssignedWithItsRoutingKeyAndEventId() throws Exception {
    String queue = "test.trip-assigned." + UUID.randomUUID();
    Binding binding = bindTestQueue(queue, "trip.assigned");
    try {
      UUID tenantId = UUID.randomUUID();
      String admin = tokens.issue(UUID.randomUUID(), tenantId, "ADMINISTRATOR", ADMIN_EMAIL);
      UUID driverUserId = UUID.randomUUID();
      fleetStub.userInvitedAsDriver(driverUserId);
      String tripId = scheduleTrip(admin);

      api.assign(admin, tripId, UUID.randomUUID().toString(), driverUserId.toString(), false)
          .andExpect(status().isOk());

      Message message = rabbitTemplate.receive(queue, 5_000);
      assertThat(message).isNotNull();
      assertThat(message.getMessageProperties().getReceivedRoutingKey()).isEqualTo("trip.assigned");
      JsonNode event = objectMapper.readTree(message.getBody());
      assertThat(UUID.fromString(event.get("eventId").asText())).isNotNull();
      assertThat(event.get("tripId").asText()).isEqualTo(tripId);
      assertThat(event.get("tenantId").asText()).isEqualTo(tenantId.toString());
      assertThat(event.get("driverId").asText()).isEqualTo(driverUserId.toString());
      assertThat(event.get("code").asText()).isEqualTo("TRP-000001");
    } finally {
      amqpAdmin.removeBinding(binding);
      amqpAdmin.deleteQueue(queue);
    }
  }

  @Test
  void processesADuplicatedSettlementClosedOnce() throws Exception {
    UUID tenantId = UUID.randomUUID();
    String admin = tokens.issue(UUID.randomUUID(), tenantId, "ADMINISTRATOR", ADMIN_EMAIL);
    UUID driverUserId = UUID.randomUUID();
    fleetStub.userInvitedAsDriver(driverUserId);
    String driver = tokens.issue(driverUserId, tenantId, "DRIVER", DRIVER_EMAIL);
    String tripId = scheduleTrip(admin);
    api.assign(admin, tripId, UUID.randomUUID().toString(), driverUserId.toString(), false)
        .andExpect(status().isOk());
    api.start(driver, tripId).andExpect(status().isOk());
    api.finish(driver, tripId).andExpect(status().isOk());

    UUID eventId = UUID.randomUUID();
    SettlementClosed closed =
        new SettlementClosed(eventId, Instant.now(), tenantId, UUID.fromString(tripId), UUID.randomUUID());
    rabbitTemplate.convertAndSend(
        RabbitConfiguration.EVENTS_EXCHANGE, RabbitConfiguration.SETTLEMENT_CLOSED_ROUTING_KEY, closed);
    rabbitTemplate.convertAndSend(
        RabbitConfiguration.EVENTS_EXCHANGE, RabbitConfiguration.SETTLEMENT_CLOSED_ROUTING_KEY, closed);
    // Un tercer mensaje distinto marca el final de la cola: con un único consumidor en orden, al
    // procesarse se sabe que los duplicados ya fueron atendidos.
    UUID sentinelId = UUID.randomUUID();
    rabbitTemplate.convertAndSend(
        RabbitConfiguration.EVENTS_EXCHANGE,
        RabbitConfiguration.SETTLEMENT_CLOSED_ROUTING_KEY,
        new SettlementClosed(sentinelId, Instant.now(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));

    Awaits.until(() -> processedCount(sentinelId) == 1, Duration.ofSeconds(10));

    assertThat(processedCount(eventId)).isEqualTo(1);
    assertThat(jdbcTemplate.queryForObject(
            "select count(*) from trip.trip_status_changes where trip_id = ? and status = 'SETTLED'",
            Integer.class,
            UUID.fromString(tripId)))
        .isEqualTo(1);
    assertThat(jdbcTemplate.queryForObject(
            "select status from trip.trips where id = ?", String.class, UUID.fromString(tripId)))
        .isEqualTo("SETTLED");
  }

  private Binding bindTestQueue(String queueName, String routingKey) {
    Queue queue = new Queue(queueName, false, true, true);
    amqpAdmin.declareQueue(queue);
    Binding binding = BindingBuilder.bind(queue).to(rindeEventsExchange).with(routingKey);
    amqpAdmin.declareBinding(binding);
    return binding;
  }

  private int processedCount(UUID eventId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "select count(*) from trip.processed_events where event_id = ?", Integer.class, eventId);
    return count == null ? 0 : count;
  }

  private String scheduleTrip(String admin) throws Exception {
    String response =
        api.schedule(admin, "Lima", "Arequipa", "Repuestos", null, LocalDate.now().plusDays(5), false)
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(response).get("id").asText();
  }
}
