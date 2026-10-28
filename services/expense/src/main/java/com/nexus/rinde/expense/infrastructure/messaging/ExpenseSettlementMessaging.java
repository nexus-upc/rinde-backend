package com.nexus.rinde.expense.infrastructure.messaging;

import com.nexus.rinde.expense.domain.model.aggregates.Expense;
import com.nexus.rinde.expense.infrastructure.persistence.jpa.repositories.ExpenseRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumidor AMQP que recibe el evento settlement.closed publicado por Settlement al cerrar una liquidación.
 * Bloquea los gastos del viaje e inserta el eventId de forma idempotente en processed_settlement_events (QA-01).
 */
@Component
public class ExpenseSettlementMessaging {

  private static final Logger log = LoggerFactory.getLogger(ExpenseSettlementMessaging.class);

  private final ExpenseRepository expenseRepository;
  private final JdbcTemplate jdbc;

  public ExpenseSettlementMessaging(ExpenseRepository expenseRepository, JdbcTemplate jdbc) {
    this.expenseRepository = expenseRepository;
    this.jdbc = jdbc;
  }

  @RabbitListener(
      queues =
          "${rinde.messaging.expense.queue:${rinde.messaging.exchange:rinde.events}.expense.settlement-events}")
  @Transactional
  public void receive(SettlementClosedEvent event, Message message) {
    if (event.eventId() == null || event.tenantId() == null || event.tripId() == null) {
      throw new IllegalArgumentException("Evento de liquidación inválido.");
    }

    int inserted =
        jdbc.update(
            "insert into expense.processed_settlement_events(event_id, processed_at) values(?, ?) on conflict do nothing",
            event.eventId(),
            Timestamp.from(Instant.now()));

    if (inserted == 0) {
      log.info("Evento de liquidación {} ya procesado previamente. Se descarta duplicado.", event.eventId());
      return;
    }

    List<Expense> expenses = expenseRepository.findByTenantIdAndTripId(event.tenantId(), event.tripId());
    for (Expense expense : expenses) {
      if (!expense.isLocked()) {
        expense.lock();
        expenseRepository.save(expense);
      }
    }
    log.info("Se bloquearon {} gastos para el viaje {} tras cierre de liquidación.", expenses.size(), event.tripId());
  }

  public record SettlementClosedEvent(
      UUID eventId,
      Instant occurredAt,
      UUID tenantId,
      UUID tripId,
      UUID closedBy) {}

  @Configuration
  public static class Topology {
    @Bean
    public Declarables expenseSettlementBindings(
        @Value("${rinde.messaging.exchange:rinde.events}") String exchange,
        @Value(
                "${rinde.messaging.expense.queue:${rinde.messaging.exchange:rinde.events}.expense.settlement-events}")
            String name) {
      var topic = new TopicExchange(exchange, true, false);
      var queue =
          QueueBuilder.durable(name)
              .deadLetterExchange(exchange + ".expense.dead")
              .deadLetterRoutingKey(name)
              .build();
      var dead = new DirectExchange(exchange + ".expense.dead", true, false);
      var deadQueue = QueueBuilder.durable(name + ".dead").build();
      return new Declarables(
          topic,
          queue,
          dead,
          deadQueue,
          BindingBuilder.bind(queue).to(topic).with("settlement.closed"),
          BindingBuilder.bind(deadQueue).to(dead).with(name));
    }
  }
}
