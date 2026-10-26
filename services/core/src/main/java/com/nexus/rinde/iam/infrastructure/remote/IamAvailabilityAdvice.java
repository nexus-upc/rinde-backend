package com.nexus.rinde.iam.infrastructure.remote;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Conserva el mismo 503 cuando una consulta HTTP de IAM falla dentro de un controlador. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IamAvailabilityAdvice {
  @ExceptionHandler(IamUnavailableException.class)
  public ProblemDetail handle(IamUnavailableException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
  }
}
