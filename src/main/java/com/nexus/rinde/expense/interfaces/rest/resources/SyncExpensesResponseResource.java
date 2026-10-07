package com.nexus.rinde.expense.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Respuesta global del proceso de sincronización en lote (US22). */
@Schema(description = "Resultado consolidado de sincronización de gastos en lote")
public record SyncExpensesResponseResource(
    @Schema(description = "Total de gastos recibidos en la solicitud", example = "3")
        int totalReceived,
    @Schema(description = "Total de gastos sincronizados exitosamente", example = "3")
        int synchronizedCount,
    @Schema(description = "Detalle de los gastos sincronizados")
        List<ExpenseResource> items) {}
