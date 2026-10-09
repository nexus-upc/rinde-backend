package com.nexus.rinde.expense.domain.model.valueobjects;

/** Gasto de un lote que no se guardó, con su clave, motivo y mensaje para el usuario. */
public record RejectedExpense(String idempotencyKey, SyncRejectionCode code, String reason) {}
