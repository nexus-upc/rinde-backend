package com.nexus.rinde.gateway.filter;

import com.nexus.rinde.gateway.security.RequestPaths;
import java.util.Optional;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Bloquea /api/internal/** (solo lo llaman otros servicios) con 404 y rechaza rutas con segmentos
 * relativos. La revisión usa la ruta decodificada, para que no haya rodeos.
 */
@Component
public class PathGuardGlobalFilter implements GlobalFilter, Ordered {

  private static final String INTERNAL_PREFIX = "/api/internal";

  private final ProblemResponseWriter problems;

  public PathGuardGlobalFilter(ProblemResponseWriter problems) {
    this.problems = problems;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    Optional<String> path = RequestPaths.canonical(exchange.getRequest().getPath().value());
    if (path.isPresent() && isInternal(path.get())) {
      return problems.write(exchange, HttpStatus.NOT_FOUND, "Recurso no encontrado.");
    }
    if (path.isEmpty()) {
      return problems.write(exchange, HttpStatus.BAD_REQUEST, "La ruta solicitada no es válida.");
    }
    return chain.filter(exchange);
  }

  private static boolean isInternal(String path) {
    return path.equals(INTERNAL_PREFIX) || path.startsWith(INTERNAL_PREFIX + "/");
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 5;
  }
}
