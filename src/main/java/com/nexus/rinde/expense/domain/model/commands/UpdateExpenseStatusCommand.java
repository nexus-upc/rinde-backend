package com.nexus.rinde.expense.domain.model.commands;

import com.nexus.rinde.expense.domain.model.valueobjects.ExpenseStatus;
import java.util.UUID;

/** Comando para aprobar u observar un gasto registrado. */
public record UpdateExpenseStatusCommand(
    UUID tenantId,
    UUID expenseId,
    UUID reviewerId,
    ExpenseStatus status,
    String reason) {}
