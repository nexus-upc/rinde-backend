package com.nexus.rinde.iam.domain.model.valueobjects;

import java.util.Objects;
import java.util.UUID;

/** Identificador de una empresa (tenant). */
public record TenantId(UUID value) {

  public TenantId {
    Objects.requireNonNull(value, "El identificador de la empresa es obligatorio.");
  }
}
