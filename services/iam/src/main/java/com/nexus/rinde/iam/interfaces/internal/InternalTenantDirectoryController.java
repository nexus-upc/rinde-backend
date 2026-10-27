package com.nexus.rinde.iam.interfaces.internal;

import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.TenantRepository;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/v1/tenant-directory")
public class InternalTenantDirectoryController {
  private final TenantRepository tenants;

  public InternalTenantDirectoryController(TenantRepository tenants) {
    this.tenants = tenants;
  }

  @GetMapping
  public List<Name> names(@RequestParam List<UUID> ids) {
    if (ids.isEmpty() || ids.size() > 100)
      throw new BusinessRuleException("Indique de 1 a 100 empresas.");
    return tenants.findAllById(ids).stream()
        .map(t -> new Name(t.getId(), t.getTradeName()))
        .toList();
  }

  public record Name(UUID id, String tradeName) {}
}
