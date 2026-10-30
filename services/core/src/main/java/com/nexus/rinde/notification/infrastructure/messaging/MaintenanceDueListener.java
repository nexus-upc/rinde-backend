package com.nexus.rinde.notification.infrastructure.messaging;

import com.nexus.rinde.fleet.interfaces.acl.MaintenanceDue;
import com.nexus.rinde.notification.application.internal.commandservices.MaintenanceDueNotificationService;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Recibe los avisos de mantenimiento que publica Fleet separado. El correo se encola una sola vez
 * por eventId, así que una entrega repetida no duplica el aviso.
 */
@Component
public class MaintenanceDueListener {

  private final MaintenanceDueNotificationService service;

  public MaintenanceDueListener(MaintenanceDueNotificationService service) {
    this.service = service;
  }

  @RabbitListener(
      queues = "${rinde.messaging.notification.maintenance-queue:notification.maintenance-due}")
  public void on(MaintenanceDue event) {
    service.handle(event);
  }

  @Configuration
  public static class Topology {

    @Bean
    public Declarables notificationMaintenanceBindings(
        @Value("${rinde.messaging.exchange:rinde.events}") String exchange,
        @Value("${rinde.messaging.notification.maintenance-queue:notification.maintenance-due}")
            String name) {
      var topic = new TopicExchange(exchange, true, false);
      var dead = new DirectExchange(RabbitMessagingConfiguration.DEAD_EXCHANGE, true, false);
      var queue =
          QueueBuilder.durable(name)
              .deadLetterExchange(RabbitMessagingConfiguration.DEAD_EXCHANGE)
              .deadLetterRoutingKey(name)
              .build();
      var deadQueue = QueueBuilder.durable(name + ".dead").build();
      return new Declarables(
          queue,
          deadQueue,
          BindingBuilder.bind(queue).to(topic).with("fleet.maintenance-due"),
          BindingBuilder.bind(deadQueue).to(dead).with(name));
    }
  }
}
