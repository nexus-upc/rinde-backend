package com.nexus.rinde.iam.interfaces.rest.transform;

import com.nexus.rinde.iam.domain.model.valueobjects.AuthenticationResult;
import com.nexus.rinde.iam.interfaces.rest.resources.AuthenticationResource;

/** Convierte el resultado del inicio de sesión en el DTO de respuesta. */
public final class AuthenticationResourceFromResultAssembler {

  private AuthenticationResourceFromResultAssembler() {}

  public static AuthenticationResource toResource(AuthenticationResult result) {
    return new AuthenticationResource(result.accessToken(), "Bearer", result.expiresInSeconds());
  }
}
