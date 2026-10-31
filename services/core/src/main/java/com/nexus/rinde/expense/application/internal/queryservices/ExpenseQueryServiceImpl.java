package com.nexus.rinde.expense.application.internal.queryservices;

import com.nexus.rinde.expense.domain.model.aggregates.Expense;
import com.nexus.rinde.expense.domain.model.queries.GetExpenseByIdQuery;
import com.nexus.rinde.expense.domain.model.queries.GetExpensesByTripQuery;
import com.nexus.rinde.expense.domain.services.ExpenseQueryService;
import com.nexus.rinde.expense.infrastructure.persistence.jpa.repositories.ExpenseRepository;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Atiende consultas sobre los gastos operativos registrados. */
@Service
public class ExpenseQueryServiceImpl implements ExpenseQueryService {

  private final ExpenseRepository expenseRepository;

  public ExpenseQueryServiceImpl(ExpenseRepository expenseRepository) {
    this.expenseRepository = expenseRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Expense> handle(GetExpensesByTripQuery query) {
    return expenseRepository.findByTenantIdAndTripId(query.tenantId(), query.tripId());
  }

  @Override
  @Transactional(readOnly = true)
  public Expense handle(GetExpenseByIdQuery query) {
    return expenseRepository
        .findByIdAndTenantId(query.expenseId(), query.tenantId())
        .orElseThrow(() -> new ResourceNotFoundException("El gasto no existe."));
  }
}
