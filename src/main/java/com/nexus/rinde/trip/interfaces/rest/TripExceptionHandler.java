package com.nexus.rinde.trip.interfaces.rest;

import com.nexus.rinde.shared.interfaces.rest.ProblemDetails;
import com.nexus.rinde.trip.application.internal.outboundservices.acl.FleetUnavailableException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Responde 503 cuando Fleet no está disponible. Va antes que el manejador compartido para que no
 * se convierta en un 500 genérico.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TripExceptionHandler {

  @ExceptionHandler(FleetUnavailableException.class)
  public ResponseEntity<ProblemDetail> handleFleetUnavailable(FleetUnavailableException ex) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(ProblemDetails.of(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage()));
  }
}
