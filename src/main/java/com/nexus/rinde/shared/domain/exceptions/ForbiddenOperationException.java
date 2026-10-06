package com.nexus.rinde.shared.domain.exceptions;

/**
 * Se lanza cuando el usuario está identificado pero no puede realizar la operación (responde 403).
 */
public class ForbiddenOperationException extends RuntimeException {

  public ForbiddenOperationException(String message) {
    super(message);
  }
}
