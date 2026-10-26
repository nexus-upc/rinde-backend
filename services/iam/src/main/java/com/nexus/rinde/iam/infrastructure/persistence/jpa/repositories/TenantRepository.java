package com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.domain.model.valueobjects.Ruc;
import java.util.UUID;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repositorio de empresas: buscar por id, validar RUC repetido y guardar. */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

  boolean existsByRuc(Ruc ruc);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from Tenant t where t.id = :id")
  Optional<Tenant> findByIdForSubscriptionUpdate(@Param("id") UUID id);
}
