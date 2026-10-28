package com.nexus.rinde.expense.domain.model.queries;

import java.util.UUID;

/** Consulta para listar los gastos de un viaje. */
public record GetExpensesByTripQuery(
    UUID tenantId,
    UUID tripId,
    UUID requesterId,
    String requesterRole) {}
