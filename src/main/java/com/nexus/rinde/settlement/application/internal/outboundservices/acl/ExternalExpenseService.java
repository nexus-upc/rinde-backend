package com.nexus.rinde.settlement.application.internal.outboundservices.acl;

import com.nexus.rinde.expense.interfaces.acl.ExpenseContextFacade;
import com.nexus.rinde.expense.interfaces.acl.ExpenseSummary;
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

  public List<ExpenseSummary> findApprovedExpensesByTripId(UUID tenantId, UUID tripId) {
    return expenseContextFacade.findApprovedExpensesByTripId(tenantId, tripId);
  }
}
