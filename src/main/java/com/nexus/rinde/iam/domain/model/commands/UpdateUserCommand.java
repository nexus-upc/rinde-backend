package com.nexus.rinde.iam.domain.model.commands;

import com.nexus.rinde.iam.domain.model.valueobjects.Role;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.model.valueobjects.UserId;

/** Cambia el rol del usuario (si viene un rol) y/o lo deshabilita, en una sola operación. */
public record UpdateUserCommand(TenantId tenantId, UserId userId, Role role, boolean disable) {}
