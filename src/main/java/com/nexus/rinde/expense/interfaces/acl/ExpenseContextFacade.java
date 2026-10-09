package com.nexus.rinde.expense.interfaces.acl;

import java.util.List;
import java.util.UUID;

/** Fachada pública del contexto Expense & Evidence para consumo de otros contextos (QA-07). */
public interface ExpenseContextFacade {

  List<ExpenseSummary> findApprovedExpensesByTripId(UUID tenantId, UUID tripId);

  List<ExpenseSummary> findByTenantIdAndTripId(UUID tenantId, UUID tripId);

  List<ExpenseSummary> findByTenantId(UUID tenantId);
}
