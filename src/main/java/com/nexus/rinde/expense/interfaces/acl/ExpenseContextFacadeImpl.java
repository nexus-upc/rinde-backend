package com.nexus.rinde.expense.interfaces.acl;

import com.nexus.rinde.expense.domain.model.aggregates.Expense;
import com.nexus.rinde.expense.domain.model.valueobjects.ExpenseStatus;
import com.nexus.rinde.expense.infrastructure.persistence.jpa.repositories.ExpenseRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Implementación de la fachada que expone los gastos aprobados sin filtrar por sesión. */
@Component
public class ExpenseContextFacadeImpl implements ExpenseContextFacade {

  private final ExpenseRepository expenseRepository;

  public ExpenseContextFacadeImpl(ExpenseRepository expenseRepository) {
    this.expenseRepository = expenseRepository;
  }

  @Override
  public List<Expense> findApprovedExpensesByTripId(UUID tripId) {
    return expenseRepository.findByTripIdAndStatus(tripId, ExpenseStatus.APPROVED);
  }

  @Override
  public List<Expense> findByTenantIdAndTripId(UUID tenantId, UUID tripId) {
    return expenseRepository.findByTenantIdAndTripId(tenantId, tripId);
  }

  @Override
  public List<Expense> findByTenantId(UUID tenantId) {
    return expenseRepository.findByTenantId(tenantId);
  }
}
