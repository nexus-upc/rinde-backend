package com.nexus.rinde.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

  private static final String SECRET = "test-secret-for-unit-tests-with-32-chars!!";

  private final JwtAuthenticationFilter filter =
      new JwtAuthenticationFilter(new JwtTokenReader(SECRET), tenantId -> Optional.empty());

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
    TenantContext.clear();
  }

  private String validToken(UUID tenantId) {
    return Jwts.builder()
        .subject(UUID.randomUUID().toString())
        .claim(JwtClaims.TENANT_ID, tenantId.toString())
        .claim(JwtClaims.ROLE, "OPERATIONS_MANAGER")
        .claim(JwtClaims.TENANT_STATUS, "ACTIVE")
        .claim(JwtClaims.EMAIL, "ops@rinde.pe")
        .expiration(Date.from(Instant.now().plusSeconds(60)))
        .signWith(JwtClaims.signingKey(SECRET))
        .compact();
  }

  @Test
  void authenticatesAndExposesTheTenantContextDuringTheRequest() throws Exception {
    UUID tenantId = UUID.randomUUID();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + validToken(tenantId));
    AtomicReference<UUID> tenantSeen = new AtomicReference<>();
    AtomicReference<Authentication> authSeen = new AtomicReference<>();

    filter.doFilter(
        request,
        new MockHttpServletResponse(),
        new MockFilterChain() {
          @Override
          public void doFilter(ServletRequest req, ServletResponse res) {
            tenantSeen.set(TenantContext.tenantId());
            authSeen.set(SecurityContextHolder.getContext().getAuthentication());
          }
        });

    assertThat(tenantSeen.get()).isEqualTo(tenantId);
    assertThat(authSeen.get().getAuthorities())
        .extracting(Object::toString)
        .containsExactly("ROLE_OPERATIONS_MANAGER");
    assertThat(TenantContext.current()).isEmpty();
  }

  @Test
  void leavesTheRequestUnauthenticatedWithoutAToken() throws Exception {
    filter.doFilter(
        new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void ignoresAnInvalidToken() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer garbage");

    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
