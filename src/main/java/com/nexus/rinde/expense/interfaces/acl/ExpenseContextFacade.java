package com.nexus.rinde.expense.interfaces.acl;

import com.nexus.rinde.expense.domain.model.aggregates.Expense;
import java.util.List;
import java.util.UUID;

/** Fachada pública del contexto Expense & Evidence para consumo de otros contextos (QA-07). */
public interface ExpenseContextFacade {

  List<Expense> findApprovedExpensesByTripId(UUID tripId);
}
