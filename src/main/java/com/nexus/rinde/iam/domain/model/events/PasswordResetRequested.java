package com.nexus.rinde.iam.domain.model.events;

import com.nexus.rinde.shared.domain.model.events.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Se publica al solicitar recuperar la contraseña; Notifications envía el enlace de recuperación.
 */
public record PasswordResetRequested(
    UUID eventId,
    Instant occurredAt,
    UUID tenantId,
    UUID userId,
    String email,
    String resetToken,
    Instant tokenExpiresAt)
    implements IntegrationEvent {

  @Override
  public String type() {
    return "PasswordResetRequested";
  }

  @Override
  public String toString() {
    return "PasswordResetRequested[eventId="
        + eventId
        + ", tenantId="
        + tenantId
        + ", userId="
        + userId
        + "]";
  }
}
