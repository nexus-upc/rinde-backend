package com.nexus.rinde.notification.infrastructure.messaging;

import com.nexus.rinde.notification.application.events.TripAssignedNotification;
import com.nexus.rinde.notification.application.internal.commandservices.TripAssignmentNotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Confirma el mensaje después de guardar el aviso; los fallos de persistencia llegan al contenedor. */
@Component
public class TripAssignedListener {

  private final TripAssignmentNotificationService service;

  public TripAssignedListener(TripAssignmentNotificationService service) {
    this.service = service;
  }

  @RabbitListener(queues = "${rinde.messaging.notification.queue:notification.trip-assigned}")
  public void on(TripAssignedNotification event) {
    service.handle(event);
  }
}
