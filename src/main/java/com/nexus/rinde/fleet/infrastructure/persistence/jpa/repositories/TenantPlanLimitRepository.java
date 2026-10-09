package com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.fleet.infrastructure.persistence.jpa.entities.TenantPlanLimit;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Proyección de límites que Fleet recibió de Subscriptions. */
@Repository
public interface TenantPlanLimitRepository extends JpaRepository<TenantPlanLimit, UUID> {}
