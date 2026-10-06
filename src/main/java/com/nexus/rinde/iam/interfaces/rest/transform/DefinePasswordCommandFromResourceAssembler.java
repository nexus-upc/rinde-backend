package com.nexus.rinde.iam.interfaces.rest.transform;

import com.nexus.rinde.iam.domain.model.commands.DefinePasswordCommand;
import com.nexus.rinde.iam.interfaces.rest.resources.DefinePasswordResource;

/** Convierte el request de definición de contraseña en el command del dominio. */
public final class DefinePasswordCommandFromResourceAssembler {

  private DefinePasswordCommandFromResourceAssembler() {}

  public static DefinePasswordCommand toCommand(DefinePasswordResource resource) {
    return new DefinePasswordCommand(resource.token(), resource.password());
  }
}
