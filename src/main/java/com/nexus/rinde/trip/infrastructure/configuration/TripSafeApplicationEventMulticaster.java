package com.nexus.rinde.trip.infrastructure.configuration;

import com.nexus.rinde.trip.domain.model.events.TripAssigned;
import com.nexus.rinde.trip.domain.model.events.TripFinished;
import com.nexus.rinde.trip.domain.model.events.TripStarted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

/** Aísla fallas de consumidores de eventos Trip para conservar la operación ya procesada. */
public class TripSafeApplicationEventMulticaster extends SimpleApplicationEventMulticaster {

  private static final Logger log =
      LoggerFactory.getLogger(TripSafeApplicationEventMulticaster.class);

  @Override
  protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
    if (isTripIntegrationEvent(event)) {
      try {
        super.invokeListener(listener, event);
      } catch (RuntimeException ex) {
        log.error("No se pudo procesar un evento de Trip Management.", ex);
      }
      return;
    }
    super.invokeListener(listener, event);
  }

  private boolean isTripIntegrationEvent(ApplicationEvent event) {
    Object payload =
        event instanceof PayloadApplicationEvent<?> payloadEvent
            ? payloadEvent.getPayload()
            : event;
    return payload instanceof TripAssigned
        || payload instanceof TripStarted
        || payload instanceof TripFinished;
  }
}
