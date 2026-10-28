package com.nexus.rinde.iam.platform.interfaces;

import com.nexus.rinde.iam.platform.application.PlatformIdentityService;
import com.nexus.rinde.shared.infrastructure.security.PlatformPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform")
@Tag(name = "Platform identity", description = "Ingreso y perfil del dueño de Rinde (US41)")
public class PlatformIdentityController {
  private final PlatformIdentityService service;

  public PlatformIdentityController(PlatformIdentityService service) {
    this.service = service;
  }

  @Operation(summary = "Iniciar sesión como dueño y obtener el token de plataforma (US41)")
  @PostMapping("/auth/sign-in")
  public Token signIn(@Valid @RequestBody Credentials body) {
    return new Token(service.signIn(body.email(), body.password()), "Bearer", 900);
  }

  @Operation(summary = "Consultar el perfil del dueño (US41)")
  @GetMapping("/me")
  @PreAuthorize("hasAuthority('ROLE_PLATFORM_OWNER')")
  public PlatformIdentityService.Profile me(@AuthenticationPrincipal PlatformPrincipal principal) {
    return service.profile(principal.id());
  }

  public record Credentials(
      @NotBlank @Email @Size(max = 150) String email, @NotBlank @Size(max = 72) String password) {}

  public record Token(String accessToken, String tokenType, long expiresInSeconds) {}
}
