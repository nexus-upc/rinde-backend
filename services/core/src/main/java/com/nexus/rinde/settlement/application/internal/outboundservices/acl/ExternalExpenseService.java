package com.nexus.rinde.settlement.application.internal.outboundservices.acl;

import com.nexus.rinde.expense.interfaces.acl.ExpenseContextFacade;
import com.nexus.rinde.expense.interfaces.acl.ExpenseSummary;
import com.nexus.rinde.settlement.domain.model.valueobjects.ApprovedExpense;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Adaptador ACL que delega en ExpenseContextFacade para obtener los gastos aprobados. */
@Service
public class ExternalExpenseService {

  private final ExpenseContextFacade expenseContextFacade;

  public ExternalExpenseService(ExpenseContextFacade expenseContextFacade) {
    this.expenseContextFacade = expenseContextFacade;
  }

  public List<ApprovedExpense> findApprovedExpensesByTripId(UUID tenantId, UUID tripId) {
    return expenseContextFacade.findApprovedExpensesByTripId(tenantId, tripId).stream()
        .map(ExternalExpenseService::toApprovedExpense)
        .toList();
  }

  private static ApprovedExpense toApprovedExpense(ExpenseSummary summary) {
    return new ApprovedExpense(
        summary.id(),
        summary.category(),
        summary.amount(),
        summary.currency(),
        summary.expenseDate(),
        summary.status());
  }
}
