package com.nexus.rinde.shared.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato base de los eventos que cruzan bounded contexts. Hoy viajan en memoria y luego viajarán
 * por el message broker con la misma estructura (CRN-08).
 */
public interface IntegrationEvent extends IdempotencyKey {

  UUID eventId();

  Instant occurredAt();

  UUID tenantId();

  /** Nombre del evento tal como lo conocen los demás contextos, por ejemplo TenantRegistered. */
  String type();

  @Override
  default String idempotencyKey() {
    return eventId().toString();
  }
}
