package com.nexus.rinde.gateway.error;

import com.nexus.rinde.gateway.filter.ProblemResponseWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Responde 503 cuando el servicio de destino rechaza la conexión o no contesta a tiempo. Se ejecuta
 * antes del manejador por defecto de Spring Boot, que respondería 500.
 */
@Component
@Order(-2)
public class UpstreamUnavailableErrorHandler implements ErrorWebExceptionHandler {

  static final String DETAIL = "El servicio no está disponible. Intente nuevamente.";

  private final ProblemResponseWriter problems;

  public UpstreamUnavailableErrorHandler(ProblemResponseWriter problems) {
    this.problems = problems;
  }

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    if (exchange.getResponse().isCommitted() || !isUpstreamUnavailable(ex)) {
      return Mono.error(ex);
    }
    return problems.write(exchange, HttpStatus.SERVICE_UNAVAILABLE, DETAIL);
  }

  /** Reconoce el rechazo de conexión y los tiempos de espera de Netty y de Spring Cloud Gateway. */
  private static boolean isUpstreamUnavailable(Throwable ex) {
    for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
      if (cause instanceof ConnectException
          || cause instanceof SocketTimeoutException
          || cause instanceof TimeoutException
          || cause instanceof io.netty.handler.timeout.TimeoutException
          || cause instanceof org.springframework.cloud.gateway.support.TimeoutException
          || isGatewayTimeout(cause)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isGatewayTimeout(Throwable cause) {
    return cause instanceof ResponseStatusException status
        && status.getStatusCode().value() == HttpStatus.GATEWAY_TIMEOUT.value();
  }
}
