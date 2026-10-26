package com.nexus.rinde.iam.interfaces.rest.transform;

import com.nexus.rinde.iam.domain.model.commands.RequestPasswordResetCommand;
import com.nexus.rinde.iam.interfaces.rest.resources.PasswordResetRequestResource;

/** Convierte el request de recuperación de contraseña en el command del dominio. */
public final class PasswordResetCommandFromResourceAssembler {

  private PasswordResetCommandFromResourceAssembler() {}

  public static RequestPasswordResetCommand toCommand(PasswordResetRequestResource resource) {
    return new RequestPasswordResetCommand(resource.email());
  }
}
