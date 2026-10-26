package com.nexus.rinde.iam.domain.model.commands;

import com.nexus.rinde.iam.domain.model.valueobjects.Role;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;

/** Invita a un usuario a la empresa con el rol indicado. */
public record InviteUserCommand(TenantId tenantId, String fullName, String email, Role role) {}
