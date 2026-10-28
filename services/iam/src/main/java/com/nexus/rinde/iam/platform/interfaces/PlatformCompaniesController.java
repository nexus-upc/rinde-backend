package com.nexus.rinde.iam.platform.interfaces;

import com.nexus.rinde.iam.platform.application.PlatformCompanyService;
import com.nexus.rinde.shared.infrastructure.security.*;
import com.nexus.rinde.shared.interfaces.rest.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/platform")
@PreAuthorize("hasAuthority('ROLE_PLATFORM_OWNER')")
@Tag(name = "Platform companies", description = "Empresas, acceso administrativo y auditoría del dueño de Rinde (US42 y US45)")
public class PlatformCompaniesController {
  private final PlatformCompanyService service;

  public PlatformCompaniesController(PlatformCompanyService service) {
    this.service = service;
  }

  @Operation(summary = "Resumen de empresas y usuarios (US42)")
  @GetMapping("/overview")
  public PlatformCompanyService.Overview overview() {
    return service.overview();
  }

  @Operation(summary = "Buscar empresas con paginación (US42)")
  @GetMapping("/companies")
  public PageResource<PlatformCompanyService.Company> companies(
      @RequestParam(defaultValue = "") String search,
      @RequestParam(required = false) Boolean suspended,
      @RequestParam(defaultValue = "0") @Min(0) @Max(1000000) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return service.list(search, suspended, page, size);
  }

  @Operation(summary = "Suspender o restablecer el acceso de una empresa con motivo (US42)")
  @PatchMapping("/companies/{id}/access")
  public PlatformCompanyService.Company access(
      @PathVariable UUID id,
      @Valid @RequestBody AccessChange body,
      @AuthenticationPrincipal PlatformPrincipal principal) {
    return service.changeAccess(id, body.suspended(), body.reason().trim(), principal);
  }

  @Operation(summary = "Auditoría de cambios de acceso (US45)")
  @GetMapping("/audit/identity")
  public PageResource<PlatformAuditTrail.Entry> audit(
      @RequestParam(defaultValue = "0") @Min(0) @Max(1000000) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return service.audit(page, size);
  }

  public record AccessChange(
      @NotNull Boolean suspended, @NotBlank @Size(min = 3, max = 500) String reason) {}
}
