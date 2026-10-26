package com.nexus.rinde.iam.interfaces.rest.transform;

import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.interfaces.rest.resources.UserResource;

/** Convierte el usuario del dominio en el DTO de respuesta. */
public final class UserResourceFromEntityAssembler {

  private UserResourceFromEntityAssembler() {}

  public static UserResource toResource(User user) {
    return new UserResource(
        user.getId(),
        user.getFullName(),
        user.getEmail().address(),
        user.getRole().name(),
        user.getStatus().name());
  }
}
