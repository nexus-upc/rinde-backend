package com.nexus.rinde.settlement.domain.model.queries;

import com.nexus.rinde.expense.domain.model.aggregates.Expense;
import com.nexus.rinde.settlement.domain.model.aggregates.Settlement;
import java.util.List;

/** Resultado de ExportSettlementQuery: la liquidación con sus gastos aprobados. */
public record SettlementExport(Settlement settlement, List<Expense> approvedExpenses) {}
