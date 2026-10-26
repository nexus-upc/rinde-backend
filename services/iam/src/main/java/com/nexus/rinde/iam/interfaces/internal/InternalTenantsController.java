package com.nexus.rinde.iam.interfaces.internal;

import com.nexus.rinde.iam.interfaces.acl.IamContextFacade;
import com.nexus.rinde.iam.interfaces.acl.UserSummary;
import com.nexus.rinde.iam.interfaces.internal.resources.ActiveUserRoleResource;
import com.nexus.rinde.iam.interfaces.internal.resources.AdministratorEmailResource;
import com.nexus.rinde.iam.interfaces.internal.resources.InternalUserResource;
import com.nexus.rinde.iam.interfaces.internal.resources.TenantStatusResource;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consultas que otros servicios hacen a IAM por HTTP. Reemplazan las llamadas directas a
 * IamContextFacade del monolito. No las enruta el gateway.
 */
@RestController
@RequestMapping(
    value = "/api/internal/v1/tenants/{tenantId}",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Internal", description = "Consultas entre servicios de RINDE (no públicas)")
@SecurityRequirements
public class InternalTenantsController {

  private final IamContextFacade iamContextFacade;

  public InternalTenantsController(IamContextFacade iamContextFacade) {
    this.iamContextFacade = iamContextFacade;
  }

  @GetMapping("/status")
  @Operation(summary = "Estado actual de la empresa")
  public TenantStatusResource tenantStatus(@PathVariable UUID tenantId) {
    return iamContextFacade
        .findTenantStatus(tenantId)
        .map(status -> new TenantStatusResource(tenantId, status))
        .orElseThrow(() -> new ResourceNotFoundException("No existe la empresa indicada."));
  }

  @GetMapping("/users/{userId}")
  @Operation(summary = "Datos de un usuario de la empresa")
  public InternalUserResource user(@PathVariable UUID tenantId, @PathVariable UUID userId) {
    return iamContextFacade
        .findUserById(tenantId, userId)
        .map(InternalTenantsController::toResource)
        .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario en la empresa."));
  }

  @GetMapping("/users/{userId}/active-role")
  @Operation(summary = "Indica si el usuario está activo y tiene el rol indicado")
  public ActiveUserRoleResource activeRole(
      @PathVariable UUID tenantId, @PathVariable UUID userId, @RequestParam String role) {
    return new ActiveUserRoleResource(
        iamContextFacade.isActiveUserWithRole(tenantId, userId, role));
  }

  @GetMapping("/administrator-email")
  @Operation(summary = "Correo del administrador activo de la empresa")
  public AdministratorEmailResource administratorEmail(@PathVariable UUID tenantId) {
    return iamContextFacade
        .findAdministratorEmail(tenantId)
        .map(AdministratorEmailResource::new)
        .orElseThrow(
            () -> new ResourceNotFoundException("La empresa no tiene un administrador activo."));
  }

  private static InternalUserResource toResource(UserSummary user) {
    return new InternalUserResource(
        user.userId(),
        user.tenantId(),
        user.fullName(),
        user.email(),
        user.role(),
        user.status());
  }
}
