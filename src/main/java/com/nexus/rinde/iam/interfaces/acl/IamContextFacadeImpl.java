package com.nexus.rinde.iam.interfaces.acl;

import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.domain.model.queries.GetTenantByIdQuery;
import com.nexus.rinde.iam.domain.model.queries.GetUserByIdQuery;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.model.valueobjects.UserId;
import com.nexus.rinde.iam.domain.model.valueobjects.UserStatus;
import com.nexus.rinde.iam.domain.services.TenantQueryService;
import com.nexus.rinde.iam.domain.services.UserQueryService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Implementa la fachada con los query services de IAM y convierte las entidades en datos simples.
 */
@Service
public class IamContextFacadeImpl implements IamContextFacade {

  private final UserQueryService userQueryService;
  private final TenantQueryService tenantQueryService;

  public IamContextFacadeImpl(
      UserQueryService userQueryService, TenantQueryService tenantQueryService) {
    this.userQueryService = userQueryService;
    this.tenantQueryService = tenantQueryService;
  }

  @Override
  public Optional<UserSummary> findUserById(UUID tenantId, UUID userId) {
    return userQueryService
        .handle(new GetUserByIdQuery(new TenantId(tenantId), new UserId(userId)))
        .map(IamContextFacadeImpl::toSummary);
  }

  @Override
  public boolean isActiveUserWithRole(UUID tenantId, UUID userId, String role) {
    return findUserById(tenantId, userId)
        .filter(user -> UserStatus.ACTIVE.name().equals(user.status()))
        .map(user -> user.role().equals(role))
        .orElse(false);
  }

  @Override
  public Optional<String> findTenantStatus(UUID tenantId) {
    return tenantQueryService
        .handle(new GetTenantByIdQuery(new TenantId(tenantId)))
        .map(tenant -> tenant.getStatus().name());
  }

  @Override
  public Optional<String> findAdministratorEmail(UUID tenantId) {
    return userQueryService
        .handle(new com.nexus.rinde.iam.domain.model.queries.GetUsersByTenantQuery(new TenantId(tenantId)))
        .stream()
        .filter(user -> "ADMINISTRATOR".equals(user.getRole().name()))
        .filter(user -> "ACTIVE".equals(user.getStatus().name()))
        .map(user -> user.getEmail().address())
        .findFirst();
  }

  private static UserSummary toSummary(User user) {
    return new UserSummary(
        user.getId(),
        user.getTenantId().value(),
        user.getFullName(),
        user.getEmail().address(),
        user.getRole().name(),
        user.getStatus().name());
  }
}
