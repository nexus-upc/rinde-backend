package com.nexus.rinde.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class JwtTokenVerifierTest {

  private static final String SECRET = "unit-test-secret-with-at-least-32-characters";

  @Test
  void acceptsTokenSignedWithSameSecret() {
    JwtTokenVerifier verifier = new JwtTokenVerifier(SECRET);

    assertThat(verifier.isValid(sign(SECRET, Instant.now().plusSeconds(600)))).isTrue();
  }

  @Test
  void rejectsTokenWithDifferentSignature() {
    JwtTokenVerifier verifier = new JwtTokenVerifier(SECRET);

    assertThat(verifier.isValid(sign("another-secret-with-at-least-32-characters", Instant.now().plusSeconds(600)))).isFalse();
  }

  @Test
  void rejectsEmptyOrGarbageTokens() {
    JwtTokenVerifier verifier = new JwtTokenVerifier(SECRET);

    assertThat(verifier.isValid("")).isFalse();
    assertThat(verifier.isValid("abc.def.ghi")).isFalse();
  }

  @Test
  void refusesShortSecretAtStartup() {
    assertThatThrownBy(() -> new JwtTokenVerifier("demasiado-corto"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("El secreto del token JWT debe tener al menos 32 caracteres.");
  }

  private static String sign(String secret, Instant expiresAt) {
    return Jwts.builder()
        .subject("7b1f6c2e-3d4a-4b5c-8d9e-0f1a2b3c4d5e")
        .expiration(Date.from(expiresAt))
        .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .compact();
  }
}
