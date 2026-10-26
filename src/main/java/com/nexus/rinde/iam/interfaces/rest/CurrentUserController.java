package com.nexus.rinde.iam.interfaces.rest;

import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.domain.model.queries.GetTenantByIdQuery;
import com.nexus.rinde.iam.domain.model.queries.GetUserByIdQuery;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.model.valueobjects.UserId;
import com.nexus.rinde.iam.domain.services.TenantQueryService;
import com.nexus.rinde.iam.domain.services.UserQueryService;
import com.nexus.rinde.iam.interfaces.rest.resources.CurrentUserResource;
import com.nexus.rinde.iam.interfaces.rest.transform.CurrentUserResourceFromEntityAssembler;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import com.nexus.rinde.shared.infrastructure.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Perfil del usuario que inicia sesión, para cualquier rol. Solo usa el usuario y la empresa del
 * token, por eso no recibe identificadores en la ruta.
 */
@RestController
@RequestMapping(value = "/api/v1/users/me", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Perfil del usuario que inicia sesión, para cualquier rol")
public class CurrentUserController {

  private final UserQueryService userQueryService;
  private final TenantQueryService tenantQueryService;

  public CurrentUserController(
      UserQueryService userQueryService, TenantQueryService tenantQueryService) {
    this.userQueryService = userQueryService;
    this.tenantQueryService = tenantQueryService;
  }

  @GetMapping
  @Operation(
      summary = "Consultar el perfil del usuario que inicia sesión",
      description = "Devuelve sus datos y los de su empresa. La web lo usa para mostrar la sesión.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Perfil del usuario y de su empresa"),
    @ApiResponse(
        responseCode = "401",
        description = "Falta el token o no es válido",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class))),
    @ApiResponse(
        responseCode = "404",
        description = "El usuario o su empresa ya no existen",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  public CurrentUserResource me() {
    TenantId tenantId = new TenantId(TenantContext.tenantId());
    User user =
        userQueryService
            .handle(new GetUserByIdQuery(tenantId, new UserId(TenantContext.userId())))
            .orElseThrow(() -> new ResourceNotFoundException("El usuario ya no existe."));
    Tenant tenant =
        tenantQueryService
            .handle(new GetTenantByIdQuery(tenantId))
            .orElseThrow(() -> new ResourceNotFoundException("La empresa ya no existe."));
    return CurrentUserResourceFromEntityAssembler.toResource(user, tenant);
  }
}
