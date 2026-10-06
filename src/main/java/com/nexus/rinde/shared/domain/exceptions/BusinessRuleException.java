package com.nexus.rinde.shared.domain.exceptions;

/**
 * Se lanza cuando una operación incumple una regla de negocio o una validación del dominio
 * (responde 400).
 */
public class BusinessRuleException extends RuntimeException {

  public BusinessRuleException(String message) {
    super(message);
  }
}
