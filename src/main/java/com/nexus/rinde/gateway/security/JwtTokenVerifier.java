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

  public JwtTokenVerifier(@Value("${rinde.security.jwt.secret}") String secret) {
    if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
      throw new IllegalStateException(
          "El secreto del token JWT debe tener al menos 32 caracteres.");
    }
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
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
