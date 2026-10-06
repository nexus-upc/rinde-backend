package com.nexus.rinde.shared.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Valida la firma y la vigencia del token JWT y lo convierte en CurrentUser. Los demás contextos
 * solo leen el token; únicamente IAM & Tenancy lo emite.
 */
@Component
public class JwtTokenReader {

  private final SecretKey key;

  public JwtTokenReader(@Value("${rinde.security.jwt.secret}") String secret) {
    this.key = JwtClaims.signingKey(secret);
  }

  /** Devuelve el usuario del token o vacío si está mal formado, vencido o con firma inválida. */
  public Optional<CurrentUser> read(String token) {
    try {
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
      return Optional.of(
          new CurrentUser(
              UUID.fromString(claims.getSubject()),
              UUID.fromString(claims.get(JwtClaims.TENANT_ID, String.class)),
              claims.get(JwtClaims.ROLE, String.class),
              claims.get(JwtClaims.TENANT_STATUS, String.class),
              claims.get(JwtClaims.EMAIL, String.class)));
    } catch (JwtException | IllegalArgumentException | NullPointerException ex) {
      return Optional.empty();
    }
  }
}
