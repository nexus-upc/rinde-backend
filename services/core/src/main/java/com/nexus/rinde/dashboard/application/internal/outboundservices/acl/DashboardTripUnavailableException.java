package com.nexus.rinde.dashboard.application.internal.outboundservices.acl;

/** El servicio Trip no respondió o devolvió un error inesperado. */
public class DashboardTripUnavailableException extends RuntimeException {

  public DashboardTripUnavailableException(Throwable cause) {
    super("El servicio de viajes no está disponible.", cause);
  }
}
