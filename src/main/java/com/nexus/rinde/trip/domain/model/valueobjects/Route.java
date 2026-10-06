package com.nexus.rinde.trip.domain.model.valueobjects;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Origen y destino del viaje. */
@Embeddable
public record Route(
    @Column(name = "origin", nullable = false, length = 120) String origin,
    @Column(name = "destination", nullable = false, length = 120) String destination) {

  public Route {
    if (origin == null || origin.isBlank() || origin.length() > 120) {
      throw new BusinessRuleException("El origen es obligatorio y admite hasta 120 caracteres.");
    }
    if (destination == null || destination.isBlank() || destination.length() > 120) {
      throw new BusinessRuleException(
          "El destino es obligatorio y admite hasta 120 caracteres.");
    }
    origin = origin.trim();
    destination = destination.trim();
  }
}
