package com.nexus.rinde.shared.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Tokens de plataforma con firma, emisor y audiencia propios. */
@Component
public class PlatformTokenReader {
  public static final String ISSUER = "rinde-platform";
  public static final String AUDIENCE = "rinde-platform-api";
  private final SecretKey key;

  public PlatformTokenReader(
      @Value("${rinde.platform.jwt.secret:}") String secret,
      @Value("${rinde.security.jwt.secret}") String tenantSecret) {
    if (!secret.isBlank() && secret.equals(tenantSecret)) {
      throw new IllegalStateException("Las claves de plataforma y empresa deben ser distintas.");
    }
    this.key = secret.isBlank() ? null : JwtClaims.signingKey(secret);
  }

  public Optional<PlatformPrincipal> read(String token) {
    if (key == null) return Optional.empty();
    try {
      Claims claims =
          Jwts.parser()
              .verifyWith(key)
              .requireIssuer(ISSUER)
              .requireAudience(AUDIENCE)
              .require("scope", "PLATFORM")
              .require("role", "PLATFORM_OWNER")
              .build()
              .parseSignedClaims(token)
              .getPayload();
      if (claims.getExpiration() == null || claims.containsKey(JwtClaims.TENANT_ID))
        return Optional.empty();
      String email = claims.get(JwtClaims.EMAIL, String.class);
      if (email == null || email.isBlank()) return Optional.empty();
      return Optional.of(new PlatformPrincipal(UUID.fromString(claims.getSubject()), email));
    } catch (RuntimeException ex) {
      return Optional.empty();
    }
  }
}
