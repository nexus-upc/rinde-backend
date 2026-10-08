package com.nexus.rinde.notification.infrastructure.services;

/** Indica que todavía no hay token de dispositivo ni proveedor push configurado. */
public class PushAdapterUnavailableException extends RuntimeException {

  public PushAdapterUnavailableException(String message) {
    super(message);
  }
}
