package com.nexus.rinde.iam.domain.model.events;

import com.nexus.rinde.shared.domain.model.events.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Se publica al invitar a un usuario; Notifications envía el correo con el enlace de invitación.
 */
public record UserInvited(
    UUID eventId,
    Instant occurredAt,
    UUID tenantId,
    UUID userId,
    String fullName,
    String email,
    String role,
    String invitationToken,
    Instant tokenExpiresAt)
    implements IntegrationEvent {

  @Override
  public String type() {
    return "UserInvited";
  }

  @Override
  public String toString() {
    return "UserInvited[eventId=" + eventId + ", tenantId=" + tenantId + ", userId=" + userId + "]";
  }
}
