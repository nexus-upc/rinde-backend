package com.nexus.rinde.settlement.domain.model.queries;

import com.nexus.rinde.expense.interfaces.acl.ExpenseSummary;
import com.nexus.rinde.settlement.domain.model.aggregates.Settlement;
import java.util.List;

/** Resultado de ExportSettlementQuery: la liquidación con sus gastos aprobados. */
public record SettlementExport(Settlement settlement, List<ExpenseSummary> approvedExpenses) {}
