package com.nexus.rinde.iam.interfaces.rest.transform;

import com.nexus.rinde.iam.domain.model.commands.RegisterTenantCommand;
import com.nexus.rinde.iam.interfaces.rest.resources.RegisterTenantResource;

/** Convierte el request de registro en el command del dominio. */
public final class RegisterTenantCommandFromResourceAssembler {

  private RegisterTenantCommandFromResourceAssembler() {}

  public static RegisterTenantCommand toCommand(RegisterTenantResource resource) {
    return new RegisterTenantCommand(
        resource.tradeName(),
        resource.ruc(),
        resource.administratorFullName(),
        resource.administratorEmail(),
        resource.password());
  }
}
