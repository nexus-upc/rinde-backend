package com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.domain.model.valueobjects.Ruc;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de empresas: buscar por id, validar RUC repetido y guardar. */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

  boolean existsByRuc(Ruc ruc);
}
