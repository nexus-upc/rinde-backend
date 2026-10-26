package com.nexus.rinde.iam.interfaces.rest.transform;

import com.nexus.rinde.iam.domain.model.commands.UpdateUserCommand;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.model.valueobjects.UserId;
import com.nexus.rinde.iam.domain.model.valueobjects.UserStatus;
import com.nexus.rinde.iam.interfaces.rest.resources.UpdateUserResource;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;

/** Convierte el PATCH del usuario en el command; del estado solo se acepta DISABLED. */
public final class UpdateUserCommandFromResourceAssembler {

  private UpdateUserCommandFromResourceAssembler() {}

  public static UpdateUserCommand toCommand(
      TenantId tenantId, UserId userId, UpdateUserResource resource) {
    if (resource.status() != null && resource.status() != UserStatus.DISABLED) {
      throw new BusinessRuleException("Solo se puede cambiar el estado a DISABLED.");
    }
    return new UpdateUserCommand(
        tenantId, userId, resource.role(), resource.status() == UserStatus.DISABLED);
  }
}
