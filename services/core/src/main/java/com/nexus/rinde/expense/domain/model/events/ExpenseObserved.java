package com.nexus.rinde.expense.domain.model.events;

import com.nexus.rinde.shared.domain.model.events.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;

/** Evento de integración publicado cuando un gasto es observado por un revisor. */
public record ExpenseObserved(
    UUID eventId,
    Instant occurredAt,
    UUID tenantId,
    UUID expenseId,
    UUID tripId,
    UUID driverId,
    String reason)
    implements IntegrationEvent {

  @Override
  public String type() {
    return "ExpenseObserved";
  }
}
