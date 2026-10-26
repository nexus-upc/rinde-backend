package com.nexus.rinde.subscription.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.nexus.rinde.subscription.interfaces.acl.SubscriptionActivated;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

/** Los fallos posteriores al commit quedan identificados para conciliación, sin simular un rollback. */
@ExtendWith(OutputCaptureExtension.class)
class SubscriptionEventPublisherTest {

  private final RabbitTemplate rabbit = mock(RabbitTemplate.class);
  private final SubscriptionEventPublisher publisher =
      new SubscriptionEventPublisher(rabbit, new TopicExchange("test.subscription.events"));

  @Test
  void recordsTheEventIdWhenPublishingFails(CapturedOutput output) {
    doThrow(new AmqpException("Broker no disponible."))
        .when(rabbit).convertAndSend(anyString(), anyString(), any(),
            any(MessagePostProcessor.class), any(CorrelationData.class));
    SubscriptionActivated event = event();

    assertThatCode(() -> publisher.on(event)).doesNotThrowAnyException();
    assertThat(output.getOut()).contains(event.eventId().toString(), "requiere conciliación");
  }

  @Test
  void recordsANegativeBrokerConfirmation(CapturedOutput output) {
    doAnswer(invocation -> {
      CorrelationData correlation = invocation.getArgument(4);
      correlation.getFuture().complete(new CorrelationData.Confirm(false, "Rechazado"));
      return null;
    }).when(rabbit).convertAndSend(anyString(), anyString(), any(),
        any(MessagePostProcessor.class), any(CorrelationData.class));
    SubscriptionActivated event = event();

    publisher.on(event);

    assertThat(output.getOut()).contains(event.eventId().toString(), "rechazó el evento");
  }

  private SubscriptionActivated event() {
    return new SubscriptionActivated(UUID.randomUUID(), Instant.now(), UUID.randomUUID());
  }
}
