package com.nexus.rinde.expense.domain.model.commands;

import java.util.UUID;

/** Comando para adjuntar evidencia fotográfica a un gasto existente. */
public record AttachEvidenceCommand(
    UUID tenantId,
    UUID expenseId,
    String imageUrl,
    Long fileSizeBytes) {}
