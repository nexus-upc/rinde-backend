package com.nexus.rinde.iam.domain.model.valueobjects;

import java.util.Objects;
import java.util.UUID;

/** Identificador de un usuario. */
public record UserId(UUID value) {

  public UserId {
    Objects.requireNonNull(value, "El identificador del usuario es obligatorio.");
  }
}
