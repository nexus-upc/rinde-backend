package com.nexus.rinde.trip.domain.model.valueobjects;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;

/** Disponibilidad técnica de la unidad y habilitación del conductor. */
public record Availability(MaintenanceState maintenance, boolean driverEnabled) {

  public Availability {
    if (maintenance == null) {
      throw new BusinessRuleException("El estado de mantenimiento es obligatorio.");
    }
  }
}
