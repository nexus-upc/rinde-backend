package com.nexus.rinde.expense.interfaces.rest;

import com.nexus.rinde.expense.domain.model.aggregates.Expense;
import com.nexus.rinde.expense.domain.model.commands.RegisterExpenseCommand;
import com.nexus.rinde.expense.domain.model.commands.UpdateExpenseStatusCommand;
import com.nexus.rinde.expense.domain.model.queries.GetExpenseByIdQuery;
import com.nexus.rinde.expense.domain.model.queries.GetExpensesByTripQuery;
import com.nexus.rinde.expense.domain.services.ExpenseCommandService;
import com.nexus.rinde.expense.domain.services.ExpenseQueryService;
import com.nexus.rinde.expense.interfaces.rest.resources.ExpenseResource;
import com.nexus.rinde.expense.interfaces.rest.resources.PresignedUrlResponseResource;
import com.nexus.rinde.expense.interfaces.rest.resources.RegisterExpenseResource;
import com.nexus.rinde.expense.interfaces.rest.resources.UpdateExpenseStatusResource;
import com.nexus.rinde.expense.interfaces.rest.transform.ExpenseResourceFromEntityAssembler;
import com.nexus.rinde.expense.interfaces.rest.transform.RegisterExpenseCommandFromResourceAssembler;
import com.nexus.rinde.expense.interfaces.rest.transform.UpdateExpenseStatusCommandFromResourceAssembler;
import com.nexus.rinde.shared.infrastructure.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Operaciones para registro, consulta y auditoría de gastos operativos y comprobantes. */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Expenses", description = "Registro de gastos, comprobantes y revisión")
@ApiResponses({
  @ApiResponse(
      responseCode = "401",
      description = "Falta el token o no es válido",
      content =
          @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))),
  @ApiResponse(
      responseCode = "403",
      description = "El rol no tiene permiso para esta operación",
      content =
          @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class)))
})
public class ExpensesController {

  private final ExpenseCommandService expenseCommandService;
  private final ExpenseQueryService expenseQueryService;

  public ExpensesController(
      ExpenseCommandService expenseCommandService, ExpenseQueryService expenseQueryService) {
    this.expenseCommandService = expenseCommandService;
    this.expenseQueryService = expenseQueryService;
  }

  @PostMapping("/api/v1/trips/{tripId}/expenses")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Registrar un gasto con su evidencia (US21)",
      description =
          "Registra un gasto operativo vinculado a un viaje. Si adjunta comprobante queda en"
              + " REGISTERED; si no se adjunta, en PENDING_SUPPORT.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Gasto registrado exitosamente"),
    @ApiResponse(
        responseCode = "400",
        description = "Monto menor o igual a cero o datos inválidos",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Clave de idempotencia duplicada",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  public ResponseEntity<ExpenseResource> registerExpense(
      @PathVariable UUID tripId, @Valid @RequestBody RegisterExpenseResource resource) {
    RegisterExpenseCommand command =
        RegisterExpenseCommandFromResourceAssembler.toCommand(
            TenantContext.tenantId(), tripId, TenantContext.userId(), resource);
    Expense expense = expenseCommandService.handle(command);
    return ResponseEntity.created(URI.create("/api/v1/expenses/" + expense.getId()))
        .body(ExpenseResourceFromEntityAssembler.toResource(expense));
  }

  @GetMapping("/api/v1/trips/{tripId}/expenses")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Consultar los gastos de un viaje (US23)",
      description = "Devuelve el listado de gastos operativos registrados para el viaje especificado.")
  @ApiResponse(responseCode = "200", description = "Lista de gastos del viaje")
  public ResponseEntity<List<ExpenseResource>> getExpensesByTrip(@PathVariable UUID tripId) {
    List<Expense> expenses =
        expenseQueryService.handle(
            new GetExpensesByTripQuery(
                TenantContext.tenantId(),
                tripId,
                TenantContext.userId(),
                TenantContext.role()));
    return ResponseEntity.ok(
        expenses.stream().map(ExpenseResourceFromEntityAssembler::toResource).toList());
  }

  @PatchMapping("/api/v1/expenses/{id}/status")
  @PreAuthorize("hasAnyRole('ADMINISTRATOR','OPERATIONS_MANAGER')")
  @Operation(
      summary = "Aprobar u observar un gasto (US24)",
      description =
          "Permite al responsable u operador aprobar un gasto o registrar observaciones"
              + " por inconsistencias.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Estado del gasto actualizado"),
    @ApiResponse(
        responseCode = "400",
        description = "Motivo de observación faltante o estado inválido",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Gasto no encontrado en la empresa",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class))),
    @ApiResponse(
        responseCode = "409",
        description = "El gasto está bloqueado por liquidación cerrada",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  public ResponseEntity<ExpenseResource> updateStatus(
      @PathVariable UUID id, @Valid @RequestBody UpdateExpenseStatusResource resource) {
    UpdateExpenseStatusCommand command =
        UpdateExpenseStatusCommandFromResourceAssembler.toCommand(
            TenantContext.tenantId(), id, TenantContext.userId(), resource);
    Expense expense = expenseCommandService.handle(command);
    return ResponseEntity.ok(ExpenseResourceFromEntityAssembler.toResource(expense));
  }

  @GetMapping("/api/v1/expenses/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Consultar el detalle de un gasto (US17)",
      description = "Devuelve los datos completos y comprobante de un gasto específico.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Detalle del gasto"),
    @ApiResponse(
        responseCode = "404",
        description = "Gasto no encontrado en la empresa",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  public ResponseEntity<ExpenseResource> getExpenseById(@PathVariable UUID id) {
    Expense expense =
        expenseQueryService.handle(new GetExpenseByIdQuery(TenantContext.tenantId(), id));
    return ResponseEntity.ok(ExpenseResourceFromEntityAssembler.toResource(expense));
  }

  @PostMapping("/api/v1/expenses/evidences/presigned-url")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Obtener un enlace firmado para subir la foto (US21)",
      description =
          "Genera un enlace de subida directa a almacenamiento de objetos (PRN-06, QA-05) sin"
              + " sobrecargar el servicio.")
  @ApiResponse(responseCode = "200", description = "Enlace firmado generado")
  public ResponseEntity<PresignedUrlResponseResource> generatePresignedUrl() {
    UUID fileId = UUID.randomUUID();
    String uploadUrl =
        "https://storage.rinde.pe/evidences/upload/" + fileId + "?signature=sig-" + UUID.randomUUID();
    String fileUrl = "https://storage.rinde.pe/evidences/" + fileId + ".jpg";
    return ResponseEntity.ok(new PresignedUrlResponseResource(uploadUrl, fileUrl, 900));
  }
}
