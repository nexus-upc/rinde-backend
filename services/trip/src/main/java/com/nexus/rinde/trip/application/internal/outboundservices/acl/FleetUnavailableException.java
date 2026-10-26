package com.nexus.rinde.trip.application.internal.outboundservices.acl;

/** Fleet no respondió o respondió algo que Trip no puede interpretar (responde 503). */
public class FleetUnavailableException extends RuntimeException {

  public FleetUnavailableException() {
    super("El servicio de flota no está disponible en este momento. Intente nuevamente más tarde.");
  }
}
