package com.nexus.rinde.shared.domain.exceptions;

/**
 * Se lanza cuando el recurso entra en conflicto con uno existente, por ejemplo un dato único
 * repetido (responde 409).
 */
public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}
