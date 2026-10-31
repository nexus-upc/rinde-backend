package com.nexus.rinde.iam.interfaces.rest.resources;

import com.nexus.rinde.iam.domain.model.valueobjects.Role;
import com.nexus.rinde.iam.domain.model.valueobjects.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/** Cambio parcial de un usuario: un nuevo rol y/o el estado DISABLED. */
public record UpdateUserResource(
    @Schema(example = "OPERATIONS_MANAGER", nullable = true) Role role,
    @Schema(
            example = "DISABLED",
            allowableValues = {"DISABLED"},
            nullable = true)
        UserStatus status) {}
