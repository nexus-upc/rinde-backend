package com.nexus.rinde.trip.domain.model.valueobjects;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/** Descripción y peso opcional de la carga. */
@Embeddable
public record Cargo(
    @Column(name = "cargo_description", nullable = false, length = 200) String description,
    @Column(name = "cargo_weight_kg", precision = 10, scale = 2) BigDecimal weightKg) {

  public Cargo {
    if (description == null || description.isBlank() || description.length() > 200) {
      throw new BusinessRuleException(
          "La descripción de la carga es obligatoria y admite hasta 200 caracteres.");
    }
    if (weightKg != null && weightKg.signum() <= 0) {
      throw new BusinessRuleException("El peso de la carga debe ser mayor que cero.");
    }
    if (weightKg != null
        && (weightKg.scale() > 2 || weightKg.precision() - weightKg.scale() > 8)) {
      throw new BusinessRuleException("El peso admite hasta 8 dígitos enteros y 2 decimales.");
    }
    description = description.trim();
  }
}
