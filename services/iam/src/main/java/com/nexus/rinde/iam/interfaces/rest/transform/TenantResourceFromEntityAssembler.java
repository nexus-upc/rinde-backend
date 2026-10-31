package com.nexus.rinde.iam.interfaces.rest.transform;

import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.interfaces.rest.resources.TenantResource;

/** Convierte la empresa del dominio en el DTO de respuesta. */
public final class TenantResourceFromEntityAssembler {

  private TenantResourceFromEntityAssembler() {}

  public static TenantResource toResource(Tenant tenant) {
    return new TenantResource(
        tenant.getId(), tenant.getTradeName(), tenant.getRuc().number(), tenant.getStatus().name());
  }
}
