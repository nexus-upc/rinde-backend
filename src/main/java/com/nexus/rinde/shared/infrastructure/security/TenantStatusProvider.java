package com.nexus.rinde.shared.infrastructure.security;

import java.util.Optional;
import java.util.UUID;

/** Fuente IAM del estado actual de la empresa para evitar usar un tenantStatus JWT obsoleto. */
public interface TenantStatusProvider {

  Optional<String> findTenantStatus(UUID tenantId);
}
