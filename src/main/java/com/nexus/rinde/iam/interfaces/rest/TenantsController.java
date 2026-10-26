package com.nexus.rinde.iam.interfaces.rest;

import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.domain.model.commands.VerifyTenantCommand;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.services.TenantCommandService;
import com.nexus.rinde.iam.interfaces.rest.resources.RegisterTenantResource;
import com.nexus.rinde.iam.interfaces.rest.resources.TenantResource;
import com.nexus.rinde.iam.interfaces.rest.resources.VerifyTenantResource;
import com.nexus.rinde.iam.interfaces.rest.transform.RegisterTenantCommandFromResourceAssembler;
import com.nexus.rinde.iam.interfaces.rest.transform.TenantResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Registro de empresas y verificación de su correo (US08). Ambos endpoints son públicos. */
@RestController
@RequestMapping(value = "/api/v1/tenants", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Tenants", description = "Registro y verificación de empresas (US08)")
@SecurityRequirements
public class TenantsController {

  private final TenantCommandService tenantCommandService;

  public TenantsController(TenantCommandService tenantCommandService) {
    this.tenantCommandService = tenantCommandService;
  }

  @PostMapping
  @Operation(
      summary = "Registrar la empresa y su administrador",
      description =
          "Crea la empresa en estado PENDING_VERIFICATION y a su administrador. El token de"
              + " verificación se registra en el log del servidor (aún no se envían correos).")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Empresa registrada"),
    @ApiResponse(
        responseCode = "400",
        description = "Datos inválidos",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class))),
    @ApiResponse(
        responseCode = "409",
        description = "El RUC o el correo ya están registrados",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  public ResponseEntity<TenantResource> register(
      @Valid @RequestBody RegisterTenantResource resource) {
    Tenant tenant =
        tenantCommandService.handle(RegisterTenantCommandFromResourceAssembler.toCommand(resource));
    URI location = URI.create("/api/v1/tenants/" + tenant.getId());
    return ResponseEntity.created(location)
        .body(TenantResourceFromEntityAssembler.toResource(tenant));
  }

  @PostMapping("/{id}/verification")
  @Operation(
      summary = "Confirmar el correo de la empresa",
      description = "Activa la empresa con el token de verificación enviado a su administrador.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Empresa activada"),
    @ApiResponse(
        responseCode = "400",
        description = "Token inválido, vencido o ya utilizado",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class))),
    @ApiResponse(
        responseCode = "404",
        description = "La empresa no existe",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  public ResponseEntity<TenantResource> verify(
      @PathVariable UUID id, @Valid @RequestBody VerifyTenantResource resource) {
    Tenant tenant =
        tenantCommandService.handle(new VerifyTenantCommand(new TenantId(id), resource.token()));
    return ResponseEntity.ok(TenantResourceFromEntityAssembler.toResource(tenant));
  }
}
