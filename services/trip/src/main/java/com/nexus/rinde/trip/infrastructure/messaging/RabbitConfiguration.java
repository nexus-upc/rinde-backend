package com.nexus.rinde.trip.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología de RabbitMQ del servicio: un exchange de tipo topic para los eventos de RINDE, la cola
 * que consume Trip y el conversor JSON. Los mensajes viajan como JSON, sin depender de clases Java.
 */
@Configuration
public class RabbitConfiguration {

  public static final String EVENTS_EXCHANGE = "rinde.events";
  public static final String SETTLEMENT_CLOSED_QUEUE = "trip.settlement-closed";
  public static final String SETTLEMENT_CLOSED_ROUTING_KEY = "settlement.closed";

  @Bean
  public TopicExchange rindeEventsExchange() {
    return new TopicExchange(EVENTS_EXCHANGE, true, false);
  }

  @Bean
  public Queue settlementClosedQueue() {
    return QueueBuilder.durable(SETTLEMENT_CLOSED_QUEUE).build();
  }

  @Bean
  public Binding settlementClosedBinding(
      Queue settlementClosedQueue, TopicExchange rindeEventsExchange) {
    return BindingBuilder.bind(settlementClosedQueue)
        .to(rindeEventsExchange)
        .with(SETTLEMENT_CLOSED_ROUTING_KEY);
  }

  /** El tipo del mensaje lo decide el consumidor por el parámetro del listener, no por un header. */
  @Bean
  public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
    Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
    converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
    return converter;
  }
}
