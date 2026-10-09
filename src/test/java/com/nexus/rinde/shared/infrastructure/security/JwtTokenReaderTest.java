package com.nexus.rinde.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenReaderTest {

  private static final String SECRET = "test-secret-for-unit-tests-with-32-chars!!";

  private final JwtTokenReader reader = new JwtTokenReader(SECRET);

  private String token(String secret, Instant expiration, UUID userId, UUID tenantId) {
    return Jwts.builder()
        .subject(userId.toString())
        .claim(JwtClaims.TENANT_ID, tenantId.toString())
        .claim(JwtClaims.ROLE, "ADMINISTRATOR")
        .claim(JwtClaims.TENANT_STATUS, "RESTRICTED")
        .claim(JwtClaims.EMAIL, "admin@rinde.pe")
        .expiration(Date.from(expiration))
        .signWith(JwtClaims.signingKey(secret))
        .compact();
  }

  @Test
  void readsAllClaimsFromAValidToken() {
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();

    CurrentUser user =
        reader.read(token(SECRET, Instant.now().plusSeconds(60), userId, tenantId)).orElseThrow();

    assertThat(user.userId()).isEqualTo(userId);
    assertThat(user.tenantId()).isEqualTo(tenantId);
    assertThat(user.role()).isEqualTo("ADMINISTRATOR");
    assertThat(user.tenantStatus()).isEqualTo("RESTRICTED");
    assertThat(user.email()).isEqualTo("admin@rinde.pe");
  }

  @Test
  void rejectsAnExpiredToken() {
    String expired =
        token(SECRET, Instant.now().minusSeconds(60), UUID.randomUUID(), UUID.randomUUID());

    assertThat(reader.read(expired)).isEmpty();
  }

  @Test
  void rejectsATokenSignedWithAnotherSecret() {
    String foreign =
        token(
            "another-secret-with-more-than-32-characters",
            Instant.now().plusSeconds(60),
            UUID.randomUUID(),
            UUID.randomUUID());

    assertThat(reader.read(foreign)).isEmpty();
  }

  @Test
  void rejectsGarbage() {
    assertThat(reader.read("not-a-token")).isEmpty();
  }

  @Test
  void refusesToStartWithAShortSecret() {
    assertThatThrownBy(() -> new JwtTokenReader("short")).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(() -> new JwtTokenReader(null)).isInstanceOf(IllegalStateException.class);
  }
}
