package com.nexus.rinde.iam.platform.application;

import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.TenantRepository;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.nexus.rinde.shared.domain.exceptions.*;
import com.nexus.rinde.shared.infrastructure.security.*;
import com.nexus.rinde.shared.interfaces.rest.PageResource;
import java.time.Clock;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformCompanyService {
  private final TenantRepository tenants;
  private final UserRepository users;
  private final PlatformAuditTrail audit;
  private final Clock clock;

  public PlatformCompanyService(
      TenantRepository tenants, UserRepository users, JdbcTemplate jdbc, Clock clock) {
    this.tenants = tenants;
    this.users = users;
    this.audit = new PlatformAuditTrail(jdbc, "iam");
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public PageResource<Company> list(String query, Boolean suspended, int page, int size) {
    if (query.length() > 100 || page < 0 || page > 1000000 || size < 1 || size > 100)
      throw new BusinessRuleException("Los filtros o la paginación no son válidos.");
    return PageResource.from(
        tenants
            .searchPlatform(
                query.trim(),
                suspended,
                PageRequest.of(page, size, Sort.by("tradeName").ascending().and(Sort.by("id"))))
            .map(Company::from));
  }

  @Transactional(readOnly = true)
  public Overview overview() {
    return new Overview(tenants.count(), tenants.countByAccessSuspendedTrue(), users.count());
  }

  @Transactional
  public Company changeAccess(UUID id, boolean suspended, String reason, PlatformPrincipal actor) {
    Tenant tenant =
        tenants
            .findByIdForSubscriptionUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("La empresa no existe."));
    if (tenant.isAccessSuspended() == suspended)
      throw new ConflictException("La empresa ya tiene ese estado de acceso.");
    boolean before = tenant.isAccessSuspended();
    tenant.changePlatformAccess(suspended);
    audit.record(
        actor,
        suspended ? "COMPANY_ACCESS_SUSPENDED" : "COMPANY_ACCESS_RESTORED",
        "COMPANY",
        id,
        reason,
        Boolean.toString(before),
        Boolean.toString(suspended),
        clock.instant());
    return Company.from(tenant);
  }

  @Transactional(readOnly = true)
  public PageResource<PlatformAuditTrail.Entry> audit(int page, int size) {
    return audit.list(page, size);
  }

  public record Overview(long companies, long suspendedCompanies, long companyUsers) {}

  public record Company(
      UUID id, String tradeName, String ruc, String status, boolean accessSuspended) {
    public static Company from(Tenant t) {
      return new Company(
          t.getId(),
          t.getTradeName(),
          t.getRuc().number(),
          t.getSubscriptionStatus().name(),
          t.isAccessSuspended());
    }
  }
}
