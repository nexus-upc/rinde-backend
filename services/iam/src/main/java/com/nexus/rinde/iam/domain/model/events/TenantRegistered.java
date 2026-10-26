package com.nexus.rinde.iam.domain.model.events;

import com.nexus.rinde.shared.domain.model.events.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;

/** Se publica al registrar una empresa; Notifications envía el correo de verificación. */
public record TenantRegistered(
    UUID eventId,
    Instant occurredAt,
    UUID tenantId,
    String tradeName,
    String administratorEmail,
    String verificationToken,
    Instant tokenExpiresAt)
    implements IntegrationEvent {

  @Override
  public String type() {
    return "TenantRegistered";
  }

  @Override
  public String toString() {
    return "TenantRegistered[eventId=" + eventId + ", tenantId=" + tenantId + "]";
  }
}
