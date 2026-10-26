package com.nexus.rinde.iam.interfaces.acl;

import java.util.Optional;
import java.util.UUID;
import com.nexus.rinde.shared.infrastructure.security.TenantStatusProvider;

/**
 * Fachada pública de IAM & Tenancy para los demás contextos. Todas las consultas se limitan a la
 * empresa indicada: un usuario de otra empresa se trata como inexistente.
 */
public interface IamContextFacade extends TenantStatusProvider {

  Optional<UserSummary> findUserById(UUID tenantId, UUID userId);

  /** Indica si el usuario existe en la empresa, está activo y tiene el rol dado (por nombre). */
  boolean isActiveUserWithRole(UUID tenantId, UUID userId, String role);

  /** Estado de la empresa (PENDING_VERIFICATION, ACTIVE o RESTRICTED), si existe. */
  @Override
  Optional<String> findTenantStatus(UUID tenantId);

  /** Correo del administrador activo para comprobantes y avisos de vencimiento. */
  Optional<String> findAdministratorEmail(UUID tenantId);
}
