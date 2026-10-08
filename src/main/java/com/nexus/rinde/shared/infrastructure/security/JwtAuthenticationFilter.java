package com.nexus.rinde.shared.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee el token Bearer de cada petición, autentica al usuario en Spring Security y deja sus datos en
 * el TenantContext mientras dura la petición. Sin token válido la petición sigue sin autenticar.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenReader tokenReader;
  private final TenantStatusProvider tenantStatusProvider;

  public JwtAuthenticationFilter(
      JwtTokenReader tokenReader, TenantStatusProvider tenantStatusProvider) {
    this.tokenReader = tokenReader;
    this.tenantStatusProvider = tenantStatusProvider;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    try {
      resolveToken(request)
          .flatMap(tokenReader::read)
          .map(this::refreshTenantStatus)
          .ifPresent(
              user -> {
                TenantContext.set(user);
                SecurityContextHolder.getContext()
                    .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.role()))));
              });
      chain.doFilter(request, response);
    } finally {
      TenantContext.clear();
    }
  }

  private CurrentUser refreshTenantStatus(CurrentUser user) {
    String status =
        tenantStatusProvider.findTenantStatus(user.tenantId()).orElse(user.tenantStatus());
    return new CurrentUser(user.userId(), user.tenantId(), user.role(), status, user.email());
  }

  private Optional<String> resolveToken(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      return Optional.of(header.substring(BEARER_PREFIX.length()).trim());
    }
    return Optional.empty();
  }
}
