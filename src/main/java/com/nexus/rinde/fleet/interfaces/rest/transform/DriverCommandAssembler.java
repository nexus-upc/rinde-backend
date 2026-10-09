package com.nexus.rinde.fleet.interfaces.rest.transform;

import com.nexus.rinde.fleet.domain.model.commands.RegisterDriverCommand;
import com.nexus.rinde.fleet.interfaces.rest.resources.CreateDriverResource;
import java.util.UUID;

/** Transformador de recurso REST a comando de creación de Driver. */
public final class DriverCommandAssembler {

  private DriverCommandAssembler() {}

  public static RegisterDriverCommand toCommand(UUID tenantId, CreateDriverResource resource) {
    return new RegisterDriverCommand(
        tenantId,
        resource.userId(),
        resource.fullName(),
        resource.documentType(),
        resource.documentNumber(),
        resource.licenseNumber(),
        resource.licenseCategory(),
        resource.licenseExpirationDate());
  }
}
