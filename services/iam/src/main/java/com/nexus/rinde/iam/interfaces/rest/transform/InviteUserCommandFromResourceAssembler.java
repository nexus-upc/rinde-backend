package com.nexus.rinde.iam.interfaces.rest.transform;

import com.nexus.rinde.iam.domain.model.commands.InviteUserCommand;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.interfaces.rest.resources.InviteUserResource;

/** Convierte el request de invitación en el command, con la empresa tomada del token. */
public final class InviteUserCommandFromResourceAssembler {

  private InviteUserCommandFromResourceAssembler() {}

  public static InviteUserCommand toCommand(TenantId tenantId, InviteUserResource resource) {
    return new InviteUserCommand(tenantId, resource.fullName(), resource.email(), resource.role());
  }
}
