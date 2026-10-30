package com.nexus.rinde.configuration.remote;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Responde 503 cuando una consulta HTTP a Trip, Fleet o Expense falla dentro de un controlador. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RemoteServiceAvailabilityAdvice {

  @ExceptionHandler(RemoteServiceUnavailableException.class)
  public ProblemDetail handle(RemoteServiceUnavailableException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
  }
}
