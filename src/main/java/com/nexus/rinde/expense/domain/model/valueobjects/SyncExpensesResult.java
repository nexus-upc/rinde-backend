package com.nexus.rinde.expense.domain.model.valueobjects;

import com.nexus.rinde.expense.domain.model.aggregates.Expense;
import java.util.List;

/** Resultado de procesar un lote offline: gastos guardados o ya existentes y gastos rechazados. */
public record SyncExpensesResult(List<Expense> synchronizedExpenses, List<RejectedExpense> rejected) {}
