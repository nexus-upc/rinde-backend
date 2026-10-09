package com.nexus.rinde.fleet.interfaces.acl;

import com.nexus.rinde.shared.domain.model.events.IntegrationEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Anuncia que una unidad tiene un mantenimiento próximo o vencido para que Notifications avise. */
public record MaintenanceDue(
    UUID eventId,
    Instant occurredAt,
    UUID tenantId,
    UUID vehicleId,
    String plateNumber,
    String maintenanceState,
    LocalDate nextMaintenanceDate)
    implements IntegrationEvent {

  @Override
  public String type() {
    return "MaintenanceDue";
  }
}
