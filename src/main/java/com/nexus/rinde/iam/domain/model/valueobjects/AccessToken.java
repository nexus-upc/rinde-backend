package com.nexus.rinde.iam.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Enlace de acceso de un solo uso para invitar a un usuario, recuperar su contraseña o verificar la
 * empresa. Solo se guarda el hash del enlace; el valor real se entrega una única vez.
 */
@Embeddable
public record AccessToken(
    @Column(name = "id", nullable = false) UUID id,
    @Column(name = "tenant_id", nullable = false) TenantId tenantId,
    @Column(name = "token_hash", nullable = false, length = 255) String value,
    @Enumerated(EnumType.STRING) @Column(name = "purpose", nullable = false, length = 20)
        TokenPurpose purpose,
    @Column(name = "expires_at", nullable = false) Instant expiresAt,
    @Column(name = "used_at") Instant usedAt,
    @Column(name = "created_at", nullable = false) Instant createdAt) {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int RAW_TOKEN_BYTES = 32;

  /** Genera un enlace nuevo con el valor real y el token que se guarda (solo con el hash). */
  public static IssuedAccessToken issue(
      TenantId tenantId, TokenPurpose purpose, Duration validity, Instant now) {
    byte[] bytes = new byte[RAW_TOKEN_BYTES];
    RANDOM.nextBytes(bytes);
    String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    AccessToken token =
        new AccessToken(
            UUID.randomUUID(), tenantId, hash(raw), purpose, now.plus(validity), null, now);
    return new IssuedAccessToken(raw, token);
  }

  /** Hash SHA-256 en hexadecimal; el valor real es aleatorio y largo, por eso no necesita sal. */
  public static String hash(String rawValue) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(rawValue.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 no está disponible en esta JVM.", ex);
    }
  }

  public boolean used() {
    return usedAt != null;
  }

  public boolean isUsable(Instant now) {
    return !used() && now.isBefore(expiresAt);
  }

  public AccessToken markUsed(Instant now) {
    return new AccessToken(id, tenantId, value, purpose, expiresAt, now, createdAt);
  }
}
