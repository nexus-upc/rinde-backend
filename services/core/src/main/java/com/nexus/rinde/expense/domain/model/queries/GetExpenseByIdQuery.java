package com.nexus.rinde.expense.domain.model.queries;

import java.util.UUID;

/** Consulta para obtener el detalle de un gasto por identificador. */
public record GetExpenseByIdQuery(UUID tenantId, UUID expenseId) {}
