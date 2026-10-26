package com.nexus.rinde.iam.infrastructure.remote;

/** Fallo de comunicación con IAM; no se confunde con un usuario o empresa inexistente. */
public class IamUnavailableException extends RuntimeException {
  public IamUnavailableException() {
    super("El servicio de identidad no está disponible. Intente nuevamente.");
  }
}
