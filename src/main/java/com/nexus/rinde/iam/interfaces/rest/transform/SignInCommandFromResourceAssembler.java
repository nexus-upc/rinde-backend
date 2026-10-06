package com.nexus.rinde.iam.interfaces.rest.transform;

import com.nexus.rinde.iam.domain.model.commands.SignInCommand;
import com.nexus.rinde.iam.interfaces.rest.resources.SignInResource;

/** Convierte el request de inicio de sesión en el command del dominio. */
public final class SignInCommandFromResourceAssembler {

  private SignInCommandFromResourceAssembler() {}

  public static SignInCommand toCommand(SignInResource resource) {
    return new SignInCommand(resource.email(), resource.password());
  }
}
