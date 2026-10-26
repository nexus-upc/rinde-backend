package com.nexus.rinde.gateway.filter;

import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Propaga X-Correlation-Id hacia los servicios y lo devuelve en la respuesta. Si falta o no es
 * válido, genera uno nuevo, igual que CorrelationIdFilter del backend.
 */
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

  public static final String HEADER = "X-Correlation-Id";
  public static final String ATTRIBUTE = "rinde.correlationId";

  private static final Pattern VALID_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String incoming = exchange.getRequest().getHeaders().getFirst(HEADER);
    String correlationId =
        incoming != null && VALID_ID.matcher(incoming).matches()
            ? incoming
            : UUID.randomUUID().toString();
    exchange.getAttributes().put(ATTRIBUTE, correlationId);
    ServerWebExchange mutated =
        exchange.mutate().request(request -> request.headers(h -> h.set(HEADER, correlationId)))
            .build();
    mutated
        .getResponse()
        .beforeCommit(
            () -> {
              mutated.getResponse().getHeaders().set(HEADER, correlationId);
              return Mono.empty();
            });
    return chain.filter(mutated);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
