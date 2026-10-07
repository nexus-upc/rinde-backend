package com.nexus.rinde.expense.domain.services;

import com.nexus.rinde.expense.domain.model.aggregates.Expense;
import com.nexus.rinde.expense.domain.model.commands.AttachEvidenceCommand;
import com.nexus.rinde.expense.domain.model.commands.RegisterExpenseCommand;
import com.nexus.rinde.expense.domain.model.commands.UpdateExpenseStatusCommand;

/** Contrato de casos de uso de modificación de gastos. */
public interface ExpenseCommandService {

  Expense handle(RegisterExpenseCommand command);

  Expense handle(UpdateExpenseStatusCommand command);

  Expense handle(AttachEvidenceCommand command);

  java.util.List<Expense> handleSync(java.util.List<RegisterExpenseCommand> commands);
}
