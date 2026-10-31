package com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.domain.model.valueobjects.Ruc;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repositorio de empresas: buscar por id, validar RUC repetido y guardar. */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

  boolean existsByRuc(Ruc ruc);

  long countByAccessSuspendedTrue();

  @Query(
      "select t from Tenant t where (:suspended is null or t.accessSuspended = :suspended) "
          + "and (:query = '' or lower(t.tradeName) like lower(concat('%', :query, '%')) "
          + "or cast(t.ruc as string) like concat('%', :query, '%'))")
  org.springframework.data.domain.Page<Tenant> searchPlatform(
      @Param("query") String query,
      @Param("suspended") Boolean suspended,
      org.springframework.data.domain.Pageable page);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from Tenant t where t.id = :id")
  Optional<Tenant> findByIdForSubscriptionUpdate(@Param("id") UUID id);
}
