package com.nexus.rinde.shared.interfaces.rest;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/** Fábrica de respuestas de error en formato RFC 7807 con el traceId de la petición. */
public final class ProblemDetails {

  public static final String TRACE_ID = "traceId";

  private ProblemDetails() {}

  public static ProblemDetail of(HttpStatus status, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(status.getReasonPhrase());
    addTraceId(problem);
    return problem;
  }

  /** Agrega el traceId tomado del MDC; si no hay correlación activa genera uno nuevo. */
  public static void addTraceId(ProblemDetail problem) {
    String traceId = MDC.get(CorrelationIdFilter.MDC_KEY);
    problem.setProperty(TRACE_ID, traceId != null ? traceId : UUID.randomUUID().toString());
  }
}
