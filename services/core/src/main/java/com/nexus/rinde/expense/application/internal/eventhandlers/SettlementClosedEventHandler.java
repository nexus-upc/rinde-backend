package com.nexus.rinde.expense.application.internal.eventhandlers;

import com.nexus.rinde.expense.domain.model.aggregates.Expense;
import com.nexus.rinde.expense.infrastructure.persistence.jpa.repositories.ExpenseRepository;
import com.nexus.rinde.settlement.interfaces.acl.SettlementClosed;
import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Bloquea los gastos del viaje cuando Settlement cierra la liquidación (PRN-05, QA-06). */
@Component("expenseSettlementClosedEventHandler")
public class SettlementClosedEventHandler {

  private final ExpenseRepository expenseRepository;

  public SettlementClosedEventHandler(ExpenseRepository expenseRepository) {
    this.expenseRepository = expenseRepository;
  }

  @EventListener
  @Transactional
  public void on(SettlementClosed event) {
    List<Expense> expenses =
        expenseRepository.findByTenantIdAndTripId(event.tenantId(), event.tripId());
    for (Expense expense : expenses) {
      if (!expense.isLocked()) {
        expense.lock();
        expenseRepository.save(expense);
      }
    }
  }
}
