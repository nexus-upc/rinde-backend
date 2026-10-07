package com.nexus.rinde.expense.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.expense.domain.model.aggregates.Expense;
import com.nexus.rinde.expense.domain.model.valueobjects.ExpenseStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de gastos operativos con aislamiento por empresa. */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

  @EntityGraph(attributePaths = "evidence")
  Optional<Expense> findByIdAndTenantId(UUID id, UUID tenantId);

  @EntityGraph(attributePaths = "evidence")
  List<Expense> findByTenantIdAndTripId(UUID tenantId, UUID tripId);

  @EntityGraph(attributePaths = "evidence")
  List<Expense> findByTripIdAndStatus(UUID tripId, ExpenseStatus status);

  boolean existsByIdempotencyKey(String idempotencyKey);

  Optional<Expense> findByIdempotencyKey(String idempotencyKey);
}
