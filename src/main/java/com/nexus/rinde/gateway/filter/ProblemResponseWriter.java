package com.nexus.rinde.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Responde errores del gateway en formato RFC 7807, igual que los servicios del backend. */
@Component
public class ProblemResponseWriter {

  private final ObjectMapper objectMapper;

  public ProblemResponseWriter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(status.getReasonPhrase());
    String traceId = exchange.getAttribute(CorrelationIdGlobalFilter.ATTRIBUTE);
    problem.setProperty("traceId", traceId != null ? traceId : UUID.randomUUID().toString());

    byte[] body;
    try {
      body = objectMapper.writeValueAsBytes(problem);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException(ex);
    }
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(status);
    response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
  }
}
