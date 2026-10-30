package com.nexus.rinde.fleet.infrastructure.messaging;

import com.nexus.rinde.fleet.application.internal.eventhandlers.PlanChangedEventHandler;
import com.nexus.rinde.fleet.interfaces.acl.PlanChanged;
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
 * Recibe el cambio de plan que publica Subscriptions en core y actualiza el límite de unidades.
 * Aplicar dos veces el mismo límite deja el mismo estado, así que una entrega repetida no afecta.
 */
@Component
public class FleetSubscriptionMessaging {

  private final PlanChangedEventHandler handler;

  public FleetSubscriptionMessaging(PlanChangedEventHandler handler) {
    this.handler = handler;
  }

  @RabbitListener(
      queues =
          "${rinde.messaging.fleet.plan-queue:${rinde.messaging.exchange:rinde.events}.fleet.subscription-events}")
  public void receive(PlanChanged event) {
    if (event.eventId() == null || event.tenantId() == null || event.planId() == null)
      throw new IllegalArgumentException("Evento de plan inválido.");
    handler.on(event);
  }

  @Configuration
  public static class Topology {
    @Bean
    public Declarables fleetSubscriptionBindings(
        @Value("${rinde.messaging.exchange:rinde.events}") String exchange,
        @Value(
                "${rinde.messaging.fleet.plan-queue:${rinde.messaging.exchange:rinde.events}.fleet.subscription-events}")
            String name) {
      var topic = new TopicExchange(exchange, true, false);
      var dead = new DirectExchange(exchange + ".fleet.dead", true, false);
      var queue =
          QueueBuilder.durable(name)
              .deadLetterExchange(exchange + ".fleet.dead")
              .deadLetterRoutingKey(name)
              .build();
      var deadQueue = QueueBuilder.durable(name + ".dead").build();
      return new Declarables(
          queue,
          deadQueue,
          BindingBuilder.bind(queue).to(topic).with("subscription.plan-changed"),
          BindingBuilder.bind(deadQueue).to(dead).with(name));
    }
  }
}
