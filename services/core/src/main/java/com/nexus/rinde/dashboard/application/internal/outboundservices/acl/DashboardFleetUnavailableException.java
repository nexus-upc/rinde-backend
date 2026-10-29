package com.nexus.rinde.dashboard.application.internal.outboundservices.acl;

/** El servicio Fleet no respondió o devolvió un error inesperado. */
public class DashboardFleetUnavailableException extends RuntimeException {

  public DashboardFleetUnavailableException(Throwable cause) {
    super("El servicio de flota no está disponible.", cause);
  }
}
