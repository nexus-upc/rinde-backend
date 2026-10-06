package com.nexus.rinde.iam.domain.model.valueobjects;

/** Resultado de iniciar sesión: el token JWT y los segundos que le quedan de vigencia. */
public record AuthenticationResult(String accessToken, long expiresInSeconds) {

  @Override
  public String toString() {
    return "AuthenticationResult[protected]";
  }
}
