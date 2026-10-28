package com.nexus.rinde.fleet.infrastructure.messaging;

import com.nexus.rinde.fleet.interfaces.acl.MaintenanceDue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publica en RabbitMQ los anuncios de mantenimiento próximo o vencido para que otros servicios
 * (como Notificaciones) puedan reaccionar.
 */
@Component
public class FleetEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(FleetEventPublisher.class);
  public static final String MAINTENANCE_DUE_ROUTING_KEY = "fleet.maintenance-due";

  private final RabbitTemplate rabbitTemplate;

  public FleetEventPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(MaintenanceDue event) {
    try {
      rabbitTemplate.convertAndSend(
          FleetRabbitConfiguration.EVENTS_EXCHANGE, MAINTENANCE_DUE_ROUTING_KEY, event);
    } catch (AmqpException ex) {
      log.error("No se pudo publicar el evento {} ({}).", event.type(), event.eventId(), ex);
    }
  }
}
