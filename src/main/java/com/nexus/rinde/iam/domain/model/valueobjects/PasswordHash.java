package com.nexus.rinde.iam.domain.model.valueobjects;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;

/** Hash BCrypt de la contraseña; nunca se guarda ni se devuelve la contraseña en texto plano. */
public record PasswordHash(String value) {

  public PasswordHash {
    if (value == null || value.isBlank()) {
      throw new BusinessRuleException("El hash de la contraseña no puede estar vacío.");
    }
  }

  @Override
  public String toString() {
    return "PasswordHash[protected]";
  }
}
