package com.nexus.rinde.shared.infrastructure.security;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

/**
 * Nombres de los claims del token y creación de la clave de firma, compartidos por emisor y
 * lectores.
 */
public final class JwtClaims {

  public static final String TENANT_ID = "tenantId";
  public static final String ROLE = "role";
  public static final String TENANT_STATUS = "tenantStatus";
  public static final String EMAIL = "email";

  private static final int MIN_SECRET_BYTES = 32;

  private JwtClaims() {}

  /** Construye la clave HMAC a partir del secreto configurado; exige al menos 32 caracteres. */
  public static SecretKey signingKey(String secret) {
    if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
      throw new IllegalStateException(
          "El secreto del token JWT debe tener al menos 32 caracteres.");
    }
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }
}
