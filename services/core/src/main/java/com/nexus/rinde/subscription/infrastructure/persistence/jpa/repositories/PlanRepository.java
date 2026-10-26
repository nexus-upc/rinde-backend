package com.nexus.rinde.subscription.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.subscription.domain.model.aggregates.Plan;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio del catálogo compartido de planes. */
@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

  List<Plan> findByActiveTrueOrderByNameAsc();

  Optional<Plan> findByPlanIdAndActiveTrue(UUID planId);
}
