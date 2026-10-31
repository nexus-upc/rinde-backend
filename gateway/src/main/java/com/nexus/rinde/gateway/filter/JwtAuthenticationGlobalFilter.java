package com.nexus.rinde.gateway.filter;

import com.nexus.rinde.gateway.security.AnonymousRequestMatcher;
import com.nexus.rinde.gateway.security.JwtTokenVerifier;
import com.nexus.rinde.gateway.security.RequestPaths;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Exige Authorization Bearer con un JWT válido en las rutas que no son públicas. El encabezado se
 * reenvía sin cambios para que los servicios lo validen de nuevo.
 */
@Component
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {

  private static final String BEARER = "Bearer ";

  private final AnonymousRequestMatcher anonymousRequests;
  private final JwtTokenVerifier tokenVerifier;
  private final ProblemResponseWriter problems;

  public JwtAuthenticationGlobalFilter(
      AnonymousRequestMatcher anonymousRequests,
      JwtTokenVerifier tokenVerifier,
      ProblemResponseWriter problems) {
    this.anonymousRequests = anonymousRequests;
    this.tokenVerifier = tokenVerifier;
    this.problems = problems;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = RequestPaths.canonical(request.getPath().value()).orElse("");
    if (anonymousRequests.matches(request.getMethod(), path)) {
      return chain.filter(exchange);
    }
    String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    String token =
        authorization != null && authorization.startsWith(BEARER)
            ? authorization.substring(BEARER.length()).trim()
            : "";
    boolean tenant = tokenVerifier.isValid(token);
    boolean platform = tokenVerifier.isValidPlatform(token);
    boolean platformPath = path.startsWith("/api/v1/platform/");
    if ((platformPath && platform) || (!platformPath && tenant)) {
      return chain.filter(exchange);
    }
    if (tenant || platform)
      return problems.write(
          exchange,
          HttpStatus.FORBIDDEN,
          "Este acceso no pertenece a ese ámbito de administración.");
    exchange.getResponse().getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
    return problems.write(exchange, HttpStatus.UNAUTHORIZED, "Token ausente o inválido.");
  }

  private boolean hasValidToken(ServerHttpRequest request) {
    String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    return header != null
        && header.startsWith(BEARER)
        && tokenVerifier.isValid(header.substring(BEARER.length()).trim());
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 10;
  }
}
