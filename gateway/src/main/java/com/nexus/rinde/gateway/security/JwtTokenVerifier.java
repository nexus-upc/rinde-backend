package com.nexus.rinde.gateway.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Comprueba firma HS256 y vigencia del JWT con el mismo secreto de IAM y el monolito. Los servicios
 * vuelven a validar el token; el gateway solo filtra lo que no puede llegar.
 */
@Component
public class JwtTokenVerifier {

  private static final int MIN_SECRET_BYTES = 32;

  private final SecretKey key;
  private final SecretKey platformKey;

  public JwtTokenVerifier(String secret) {
    this(secret, "");
  }

  @org.springframework.beans.factory.annotation.Autowired
  public JwtTokenVerifier(
      @Value("${rinde.security.jwt.secret}") String secret,
      @Value("${rinde.platform.jwt.secret:}") String platformSecret) {
    if (!platformSecret.isBlank() && platformSecret.equals(secret))
      throw new IllegalStateException("Las claves de plataforma y empresa deben ser distintas.");
    this.platformKey =
        platformSecret.isBlank()
            ? null
            : Keys.hmacShaKeyFor(platformSecret.getBytes(StandardCharsets.UTF_8));
    if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
      throw new IllegalStateException(
          "El secreto del token JWT debe tener al menos 32 caracteres.");
    }
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public boolean isValidPlatform(String token) {
    if (platformKey == null) return false;
    try {
      var claims =
          Jwts.parser()
              .verifyWith(platformKey)
              .requireIssuer("rinde-platform")
              .requireAudience("rinde-platform-api")
              .require("scope", "PLATFORM")
              .require("role", "PLATFORM_OWNER")
              .build()
              .parseSignedClaims(token)
              .getPayload();
      return claims.getExpiration() != null
          && !claims.containsKey("tenantId")
          && claims.getSubject() != null;
    } catch (RuntimeException ex) {
      return false;
    }
  }

  /** Devuelve true si el token tiene firma válida y no está vencido. */
  public boolean isValid(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException ex) {
      return false;
    }
  }
}
