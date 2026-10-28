package com.nexus.rinde.expense.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Respuesta global del proceso de sincronización en lote (US22). */
@Schema(description = "Resultado consolidado de sincronización de gastos en lote")
public record SyncExpensesResponseResource(
    @Schema(description = "Total de gastos recibidos en la solicitud", example = "3")
        int totalReceived,
    @Schema(
            description =
                "Total de gastos guardados en esta solicitud o ya guardados antes con la misma clave",
            example = "2")
        int synchronizedCount,
    @Schema(description = "Detalle de los gastos sincronizados o ya existentes")
        List<ExpenseResource> items,
    @Schema(description = "Total de gastos rechazados en la solicitud", example = "1")
        int rejectedCount,
    @Schema(description = "Detalle de los gastos rechazados, uno por cada ítem no guardado")
        List<SyncRejectedItemResource> rejected) {}
