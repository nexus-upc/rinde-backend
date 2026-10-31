package com.nexus.rinde.iam.infrastructure.messaging;

import com.nexus.rinde.iam.application.events.SubscriptionStatusMessage;
import com.nexus.rinde.iam.application.internal.eventhandlers.SubscriptionMessageHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/** El DTO se infiere localmente, sin importar la clase Java del productor. */
@Component
public class SubscriptionStatusListener {
  private final SubscriptionMessageHandler handler;

  public SubscriptionStatusListener(SubscriptionMessageHandler handler) {
    this.handler = handler;
  }

  @RabbitListener(queues = "${rinde.messaging.subscription.queue:iam.subscription-events}")
  public void on(SubscriptionStatusMessage event, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String key) {
    handler.handle(event, key);
  }
}
