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

  public JwtAuthenticationFilter(JwtTokenReader tokenReader) {
    this.tokenReader = tokenReader;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    try {
      resolveToken(request)
          .flatMap(tokenReader::read)
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

  private Optional<String> resolveToken(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      return Optional.of(header.substring(BEARER_PREFIX.length()).trim());
    }
    return Optional.empty();
  }
}
