package com.nexus.rinde.iam.application.internal.queryservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.domain.model.queries.GetTenantByIdQuery;
import com.nexus.rinde.iam.domain.model.valueobjects.Ruc;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.TenantRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantQueryServiceImplTest {

  @Mock private TenantRepository tenantRepository;
  @InjectMocks private TenantQueryServiceImpl service;

  @Test
  void findsATenantById() {
    Tenant tenant = new Tenant("Transportes Andes", new Ruc("20123456789"));
    when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));

    assertThat(service.handle(new GetTenantByIdQuery(tenant.getTenantId()))).contains(tenant);
  }

  @Test
  void returnsEmptyForAnUnknownTenant() {
    UUID unknown = UUID.randomUUID();
    when(tenantRepository.findById(unknown)).thenReturn(Optional.empty());

    assertThat(service.handle(new GetTenantByIdQuery(new TenantId(unknown)))).isEmpty();
  }
}
