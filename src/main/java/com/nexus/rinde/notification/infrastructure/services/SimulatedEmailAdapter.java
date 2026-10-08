package com.nexus.rinde.notification.infrastructure.services;

import com.nexus.rinde.notification.domain.model.valueobjects.EmailMessage;
import com.nexus.rinde.notification.domain.services.EmailAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Simula la entrega para pruebas locales; no se conecta a un servidor de correo. */
@Component
public class SimulatedEmailAdapter implements EmailAdapter {

  private static final Logger log = LoggerFactory.getLogger(SimulatedEmailAdapter.class);

  @Override
  public void send(EmailMessage message) {
    log.info(
        "Correo simulado enviado a {} para la empresa {}: {}",
        message.recipient(),
        message.tenantId(),
        message.subject());
  }
}
