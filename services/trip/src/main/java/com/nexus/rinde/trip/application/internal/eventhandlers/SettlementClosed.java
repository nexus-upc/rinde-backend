package com.nexus.rinde.trip.application.internal.eventhandlers;

import com.nexus.rinde.shared.domain.model.events.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Cierre de liquidación que publica Settlement en settlement.closed. Trip conserva su propia copia
 * del contrato; los campos coinciden con el mensaje que envía el monolito.
 */
public record SettlementClosed(
    UUID eventId, Instant occurredAt, UUID tenantId, UUID tripId, UUID closedBy)
    implements IntegrationEvent {

  @Override
  public String type() {
    return "SettlementClosed";
  }
}
