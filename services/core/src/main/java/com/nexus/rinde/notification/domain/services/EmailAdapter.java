package com.nexus.rinde.notification.domain.services;

import com.nexus.rinde.notification.domain.model.valueobjects.EmailMessage;

/** Puerto de salida para comprobantes y avisos de suscripción. */
public interface EmailAdapter {

  void send(EmailMessage message);
}
