package com.nexus.rinde.trip.infrastructure.messaging;

import com.nexus.rinde.trip.application.internal.eventhandlers.SettlementClosed;
import com.nexus.rinde.trip.application.internal.eventhandlers.SettlementClosedEventHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Recibe settlement.closed desde la cola de Trip. Un mensaje que no se puede procesar se rechaza sin
 * reencolar, para que un error permanente no se repita sin fin.
 */
@Component
public class SettlementClosedListener {

  private final SettlementClosedEventHandler handler;

  public SettlementClosedListener(SettlementClosedEventHandler handler) {
    this.handler = handler;
  }

  @RabbitListener(queues = RabbitConfiguration.SETTLEMENT_CLOSED_QUEUE)
  public void on(SettlementClosed event) {
    handler.handle(event);
  }
}
