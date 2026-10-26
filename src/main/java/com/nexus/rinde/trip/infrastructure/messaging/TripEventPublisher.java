package com.nexus.rinde.trip.infrastructure.messaging;

import com.nexus.rinde.shared.domain.model.events.IntegrationEvent;
import com.nexus.rinde.trip.domain.model.events.TripAssigned;
import com.nexus.rinde.trip.domain.model.events.TripFinished;
import com.nexus.rinde.trip.domain.model.events.TripStarted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publica los eventos de Trip en el exchange rinde.events cuando el viaje ya quedó guardado. Si el
 * broker no responde, el cambio se conserva y el fallo queda en el log.
 */
@Component
public class TripEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(TripEventPublisher.class);
  static final String TRIP_ASSIGNED_ROUTING_KEY = "trip.assigned";
  static final String TRIP_STARTED_ROUTING_KEY = "trip.started";
  static final String TRIP_FINISHED_ROUTING_KEY = "trip.finished";

  private final RabbitTemplate rabbitTemplate;

  public TripEventPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(TripAssigned event) {
    publish(TRIP_ASSIGNED_ROUTING_KEY, event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(TripStarted event) {
    publish(TRIP_STARTED_ROUTING_KEY, event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(TripFinished event) {
    publish(TRIP_FINISHED_ROUTING_KEY, event);
  }

  private void publish(String routingKey, IntegrationEvent event) {
    try {
      rabbitTemplate.convertAndSend(RabbitConfiguration.EVENTS_EXCHANGE, routingKey, event);
    } catch (AmqpException ex) {
      log.error("No se pudo publicar el evento {} ({}).", event.type(), event.eventId(), ex);
    }
  }
}
