package com.nexus.rinde.iam.demomail.domain;

import com.nexus.rinde.iam.domain.model.valueobjects.TokenPurpose;
import java.time.Instant;
import java.util.UUID;

/** Metadatos de un correo simulado; el token se mantiene cifrado fuera de la respuesta. */
public record DemoMail(
    UUID id,
    UUID eventId,
    UUID tenantId,
    String recipient,
    String recipientName,
    String companyName,
    TokenPurpose purpose,
    String encryptedToken,
    Instant createdAt,
    Instant expiresAt,
    Instant readAt,
    boolean tokenExists,
    Instant usedAt) {
  public String state(Instant now) {
    if (!tokenExists) return "INVALID";
    if (usedAt != null) return "USED";
    if (!now.isBefore(expiresAt)) return "EXPIRED";
    return "ACTIVE";
  }

  public String encryptionContext() {
    return eventId + "|" + tenantId + "|" + purpose.name();
  }

  @Override
  public String toString() {
    return "DemoMail[id=" + id + ", purpose=" + purpose + "]";
  }
}
