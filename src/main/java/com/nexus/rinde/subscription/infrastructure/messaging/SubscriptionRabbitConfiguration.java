package com.nexus.rinde.subscription.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Declara también la cola receptora para conservar eventos aunque IAM todavía no haya arrancado. */
@Configuration
public class SubscriptionRabbitConfiguration {

  @Bean
  public DirectExchange iamDeadExchange() {
    return new DirectExchange("rinde.iam.dead", true, false);
  }

  @Bean
  public Queue iamSubscriptionQueue(
      @Value("${rinde.messaging.subscription.queue:iam.subscription-events}") String name) {
    return QueueBuilder.durable(name).deadLetterExchange("rinde.iam.dead")
        .deadLetterRoutingKey(name).build();
  }

  @Bean
  public Queue iamSubscriptionDeadQueue(
      @Value("${rinde.messaging.subscription.queue:iam.subscription-events}") String name) {
    return QueueBuilder.durable(name + ".dead").build();
  }

  @Bean
  public Binding subscriptionActivatedBinding(@Qualifier("iamSubscriptionQueue") Queue queue,
      TopicExchange rindeEventsExchange) {
    return BindingBuilder.bind(queue).to(rindeEventsExchange).with("subscription.activated");
  }

  @Bean
  public Binding subscriptionSuspendedBinding(@Qualifier("iamSubscriptionQueue") Queue queue,
      TopicExchange rindeEventsExchange) {
    return BindingBuilder.bind(queue).to(rindeEventsExchange).with("subscription.suspended");
  }

  @Bean
  public Binding subscriptionDeadBinding(@Qualifier("iamSubscriptionDeadQueue") Queue queue,
      DirectExchange iamDeadExchange,
      @Value("${rinde.messaging.subscription.queue:iam.subscription-events}") String name) {
    return BindingBuilder.bind(queue).to(iamDeadExchange).with(name);
  }
}
