package com.nexus.rinde.expense.interfaces.rest.resources;

import com.nexus.rinde.expense.domain.model.valueobjects.ExpenseCategory;
import com.nexus.rinde.expense.domain.model.valueobjects.ExpenseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Representación pública de un gasto operativo. */
public record ExpenseResource(
    @Schema(example = "3c589391-d520-4d01-838a-1a574c7d2bb6") UUID id,
    @Schema(example = "d011f0a2-2b6f-4d32-9cb4-fa6d7cb81698") UUID tenantId,
    @Schema(example = "87460614-42ea-4cd5-9de1-8f38d9496434") UUID tripId,
    @Schema(example = "764a4968-403f-40d7-9172-ba637d10fb16") UUID driverId,
    @Schema(example = "FUEL") ExpenseCategory category,
    @Schema(example = "180.50") BigDecimal amount,
    @Schema(example = "PEN") String currency,
    @Schema(example = "2026-10-20") LocalDate expenseDate,
    @Schema(example = "REGISTERED") ExpenseStatus status,
    @Schema(example = "EXP-9831-UUID-01") String idempotencyKey,
    @Schema(example = "null", nullable = true) String observationReason,
    @Schema(example = "false") boolean isLocked,
    @Schema(nullable = true) EvidenceResource evidence,
    Instant createdAt,
    Instant updatedAt) {}
