package com.nexus.rinde.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Declara la cola durable y conserva los mensajes rechazados en una cola de errores. */
@Configuration
public class RabbitMessagingConfiguration {

  public static final String EVENTS_EXCHANGE = "rinde.events";
  public static final String DEAD_EXCHANGE = "rinde.notification.dead";

  @Bean
  public TopicExchange rindeEventsExchange(
      @Value("${rinde.messaging.exchange:rinde.events}") String name) {
    return new TopicExchange(name, true, false);
  }

  @Bean
  public DirectExchange notificationDeadExchange() {
    return new DirectExchange(DEAD_EXCHANGE, true, false);
  }

  @Bean
  public Queue notificationTripAssignedQueue(
      @Value("${rinde.messaging.notification.queue:notification.trip-assigned}") String name) {
    return QueueBuilder.durable(name)
        .deadLetterExchange(DEAD_EXCHANGE).deadLetterRoutingKey(name).build();
  }

  @Bean
  public Queue notificationDeadQueue(
      @Value("${rinde.messaging.notification.queue:notification.trip-assigned}") String name) {
    return QueueBuilder.durable(name + ".dead").build();
  }

  @Bean
  public Binding notificationTripAssignedBinding(
      @Qualifier("notificationTripAssignedQueue") Queue queue, TopicExchange rindeEventsExchange) {
    return BindingBuilder.bind(queue).to(rindeEventsExchange).with("trip.assigned");
  }

  @Bean
  public Binding notificationDeadBinding(
      @Qualifier("notificationDeadQueue") Queue queue, DirectExchange notificationDeadExchange,
      @Value("${rinde.messaging.notification.queue:notification.trip-assigned}") String name) {
    return BindingBuilder.bind(queue).to(notificationDeadExchange).with(name);
  }

  @Bean
  public MessageConverter jsonMessageConverter(ObjectMapper mapper) {
    var converter = new Jackson2JsonMessageConverter(mapper);
    converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
    return converter;
  }
}
