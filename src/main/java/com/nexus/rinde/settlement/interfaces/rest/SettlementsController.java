package com.nexus.rinde.settlement.interfaces.rest;

import com.nexus.rinde.settlement.domain.model.aggregates.Settlement;
import com.nexus.rinde.settlement.domain.model.commands.RegisterAdvanceCommand;
import com.nexus.rinde.settlement.domain.model.queries.GetSettlementByIdQuery;
import com.nexus.rinde.settlement.domain.model.queries.GetSettlementByTripIdQuery;
import com.nexus.rinde.settlement.domain.services.SettlementCommandService;
import com.nexus.rinde.settlement.domain.services.SettlementQueryService;
import com.nexus.rinde.settlement.interfaces.rest.resources.RegisterAdvanceResource;
import com.nexus.rinde.settlement.interfaces.rest.resources.SettlementResource;
import com.nexus.rinde.settlement.interfaces.rest.transform.RegisterAdvanceCommandFromResourceAssembler;
import com.nexus.rinde.settlement.interfaces.rest.transform.SettlementResourceFromEntityAssembler;
import com.nexus.rinde.shared.infrastructure.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Operaciones de consulta y gestión de liquidaciones de viaje. */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Settlements", description = "Liquidaciones de viaje y anticipos")
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
public class SettlementsController {

  private final SettlementCommandService settlementCommandService;
  private final SettlementQueryService settlementQueryService;

  public SettlementsController(
      SettlementCommandService settlementCommandService,
      SettlementQueryService settlementQueryService) {
    this.settlementCommandService = settlementCommandService;
    this.settlementQueryService = settlementQueryService;
  }

  @PostMapping("/api/v1/settlements/advances")
  @PreAuthorize("hasAnyRole('ADMINISTRATOR','OPERATIONS_MANAGER')")
  @Operation(
      summary = "Registrar anticipo al conductor (US35)",
      description = "Registra el monto anticipado al conductor para cubrir los gastos del viaje.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Anticipo registrado exitosamente"),
    @ApiResponse(
        responseCode = "400",
        description = "Monto menor o igual a cero o datos inválidos",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No existe liquidación para el viaje indicado",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  public ResponseEntity<SettlementResource> registerAdvance(
      @Valid @RequestBody RegisterAdvanceResource resource) {
    RegisterAdvanceCommand command =
        RegisterAdvanceCommandFromResourceAssembler.toCommand(
            TenantContext.tenantId(), TenantContext.userId(), resource);
    Settlement settlement = settlementCommandService.handle(command);
    return ResponseEntity.ok(SettlementResourceFromEntityAssembler.toResource(settlement));
  }

  @GetMapping("/api/v1/settlements/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Consultar el detalle de una liquidación (US25)",
      description = "Devuelve los datos completos de una liquidación por su identificador.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Detalle de la liquidación"),
    @ApiResponse(
        responseCode = "404",
        description = "Liquidación no encontrada",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  public ResponseEntity<SettlementResource> getSettlementById(@PathVariable UUID id) {
    Settlement settlement =
        settlementQueryService.handle(
            new GetSettlementByIdQuery(TenantContext.tenantId(), id));
    return ResponseEntity.ok(SettlementResourceFromEntityAssembler.toResource(settlement));
  }

  @GetMapping("/api/v1/trips/{tripId}/settlement")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Consultar la liquidación de un viaje (US28)",
      description = "Devuelve la liquidación asociada al viaje especificado.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Liquidación del viaje"),
    @ApiResponse(
        responseCode = "404",
        description = "No existe liquidación para este viaje",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  public ResponseEntity<SettlementResource> getSettlementByTrip(@PathVariable UUID tripId) {
    Settlement settlement =
        settlementQueryService.handle(
            new GetSettlementByTripIdQuery(TenantContext.tenantId(), tripId));
    return ResponseEntity.ok(SettlementResourceFromEntityAssembler.toResource(settlement));
  }
}
