package com.nexus.rinde.iam.interfaces.rest.transform;

import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.interfaces.rest.resources.CurrentUserResource;

/** Convierte el usuario y su empresa del dominio en el perfil de respuesta. */
public final class CurrentUserResourceFromEntityAssembler {

  private CurrentUserResourceFromEntityAssembler() {}

  public static CurrentUserResource toResource(User user, Tenant tenant) {
    return new CurrentUserResource(
        user.getId(),
        user.getFullName(),
        user.getEmail().address(),
        user.getRole().name(),
        user.getStatus().name(),
        TenantResourceFromEntityAssembler.toResource(tenant));
  }
}
