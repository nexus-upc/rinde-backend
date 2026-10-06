package com.nexus.rinde.shared.domain.exceptions;

/**
 * Se lanza cuando un recurso no existe o no es visible para la empresa del usuario (responde 404).
 */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
