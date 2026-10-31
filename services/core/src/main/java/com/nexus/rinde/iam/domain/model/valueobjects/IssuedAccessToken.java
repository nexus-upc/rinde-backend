package com.nexus.rinde.iam.domain.model.valueobjects;

/**
 * Enlace recién emitido: el valor real (que solo se entrega una vez) y el token que se persiste.
 */
public record IssuedAccessToken(String rawValue, AccessToken token) {

  @Override
  public String toString() {
    return "IssuedAccessToken[protected]";
  }
}
