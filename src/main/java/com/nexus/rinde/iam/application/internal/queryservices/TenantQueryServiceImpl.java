package com.nexus.rinde.iam.application.internal.queryservices;

import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.domain.model.queries.GetTenantByIdQuery;
import com.nexus.rinde.iam.domain.services.TenantQueryService;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.TenantRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta empresas por id. */
@Service
public class TenantQueryServiceImpl implements TenantQueryService {

  private final TenantRepository tenantRepository;

  public TenantQueryServiceImpl(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Tenant> handle(GetTenantByIdQuery query) {
    return tenantRepository.findById(query.tenantId().value());
  }
}
