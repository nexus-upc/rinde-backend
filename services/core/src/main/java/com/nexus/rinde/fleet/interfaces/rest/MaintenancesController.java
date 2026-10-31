package com.nexus.rinde.fleet.interfaces.rest;

import com.nexus.rinde.fleet.domain.model.commands.RecordMaintenanceCommand;
import com.nexus.rinde.fleet.domain.model.entities.Maintenance;
import com.nexus.rinde.fleet.domain.model.queries.ListMaintenanceAlertsQuery;
import com.nexus.rinde.fleet.domain.model.valueobjects.MaintenanceAlert;
import com.nexus.rinde.fleet.domain.services.MaintenanceCommandService;
import com.nexus.rinde.fleet.domain.services.MaintenanceQueryService;
import com.nexus.rinde.fleet.interfaces.rest.resources.MaintenanceAlertResource;
import com.nexus.rinde.fleet.interfaces.rest.resources.MaintenanceResource;
import com.nexus.rinde.fleet.interfaces.rest.resources.RecordMaintenanceResource;
import com.nexus.rinde.fleet.interfaces.rest.transform.MaintenanceResourceAssembler;
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
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Operaciones y alertas sobre el mantenimiento de vehículos de la flota. */
@RestController
@RequestMapping(value = "/api/v1/maintenances", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Maintenances", description = "Registro de mantenimientos y alertas de la flota")
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
public class MaintenancesController {

  private final MaintenanceCommandService maintenanceCommandService;
  private final MaintenanceQueryService maintenanceQueryService;

  public MaintenancesController(
      MaintenanceCommandService maintenanceCommandService,
      MaintenanceQueryService maintenanceQueryService) {
    this.maintenanceCommandService = maintenanceCommandService;
    this.maintenanceQueryService = maintenanceQueryService;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMINISTRATOR','OPERATIONS_MANAGER')")
  @Operation(
      summary = "Registrar un mantenimiento (US33)",
      description = "Registra un servicio de mantenimiento realizado y programa el próximo.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Mantenimiento registrado"),
    @ApiResponse(
        responseCode = "400",
        description = "Datos inválidos",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class))),
    @ApiResponse(
        responseCode = "404",
        description = "El vehículo no existe en la empresa",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  public ResponseEntity<MaintenanceResource> create(
      @Valid @RequestBody RecordMaintenanceResource resource) {
    RecordMaintenanceCommand command =
        MaintenanceResourceAssembler.toCommand(TenantContext.tenantId(), null, resource);
    Maintenance maintenance = maintenanceCommandService.handle(command);
    return ResponseEntity.created(URI.create("/api/v1/maintenances/" + maintenance.getId()))
        .body(MaintenanceResourceAssembler.toResource(maintenance));
  }

  @GetMapping("/alerts")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Consultar alertas de mantenimiento pendiente (US34)",
      description =
          "Devuelve todas las unidades con mantenimientos próximos (dentro de 7 días) o vencidos.")
  @ApiResponse(responseCode = "200", description = "Lista de alertas de mantenimiento")
  public ResponseEntity<List<MaintenanceAlertResource>> listAlerts() {
    List<MaintenanceAlert> alerts =
        maintenanceQueryService.handle(new ListMaintenanceAlertsQuery(TenantContext.tenantId()));
    return ResponseEntity.ok(
        alerts.stream().map(MaintenanceResourceAssembler::toAlertResource).toList());
  }
}
