package com.nexus.rinde.shared.domain.exceptions;

/**
 * Se lanza cuando no se puede identificar al usuario, por ejemplo con credenciales incorrectas
 * (responde 401).
 */
public class AuthenticationFailedException extends RuntimeException {

  public AuthenticationFailedException(String message) {
    super(message);
  }
}
