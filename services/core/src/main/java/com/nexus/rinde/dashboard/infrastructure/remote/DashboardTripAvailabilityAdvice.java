package com.nexus.rinde.dashboard.infrastructure.remote;

import com.nexus.rinde.dashboard.application.internal.outboundservices.acl.DashboardTripUnavailableException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Devuelve 503 cuando el servicio Trip no responde durante una consulta del dashboard. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DashboardTripAvailabilityAdvice {

  @ExceptionHandler(DashboardTripUnavailableException.class)
  public ProblemDetail handle(DashboardTripUnavailableException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
  }
}
