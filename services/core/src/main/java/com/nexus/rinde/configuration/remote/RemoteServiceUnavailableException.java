package com.nexus.rinde.configuration.remote;

/** Fallo de comunicación con otro servicio; no se confunde con un recurso inexistente. */
public class RemoteServiceUnavailableException extends RuntimeException {

  public RemoteServiceUnavailableException(String service, Throwable cause) {
    super("El servicio de " + service + " no está disponible. Intente nuevamente.", cause);
  }
}
