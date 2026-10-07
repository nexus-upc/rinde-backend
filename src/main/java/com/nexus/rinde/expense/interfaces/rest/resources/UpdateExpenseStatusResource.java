package com.nexus.rinde.expense.interfaces.rest.resources;

import com.nexus.rinde.expense.domain.model.valueobjects.ExpenseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para aprobar u observar un gasto registrado. */
public record UpdateExpenseStatusResource(
    @Schema(example = "APPROVED", allowableValues = {"APPROVED", "OBSERVED"})
        @NotNull
        ExpenseStatus status,
    @Schema(example = "Comprobante ilegible o borroso", nullable = true)
        @Size(max = 255)
        String reason) {}
