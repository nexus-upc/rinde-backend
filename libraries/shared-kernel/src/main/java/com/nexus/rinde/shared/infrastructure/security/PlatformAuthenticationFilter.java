package com.nexus.rinde.shared.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Autentica exclusivamente las rutas administrativas de plataforma. */
public class PlatformAuthenticationFilter extends OncePerRequestFilter {
  private final PlatformTokenReader reader;

  public PlatformAuthenticationFilter(PlatformTokenReader reader) {
    this.reader = reader;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request
        .getRequestURI()
        .substring(request.getContextPath().length())
        .startsWith("/api/v1/platform/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      reader
          .read(header.substring(7).trim())
          .ifPresent(
              principal ->
                  SecurityContextHolder.getContext()
                      .setAuthentication(
                          new UsernamePasswordAuthenticationToken(
                              principal,
                              null,
                              List.of(new SimpleGrantedAuthority(PlatformPrincipal.AUTHORITY)))));
    }
    chain.doFilter(request, response);
  }
}
