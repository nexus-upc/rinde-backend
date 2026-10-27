package com.nexus.rinde.iam.platform.interfaces;

import com.nexus.rinde.iam.platform.application.PlatformIdentityService;
import com.nexus.rinde.shared.infrastructure.security.PlatformPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform")
public class PlatformIdentityController {
  private final PlatformIdentityService service;

  public PlatformIdentityController(PlatformIdentityService service) {
    this.service = service;
  }

  @PostMapping("/auth/sign-in")
  public Token signIn(@Valid @RequestBody Credentials body) {
    return new Token(service.signIn(body.email(), body.password()), "Bearer", 900);
  }

  @GetMapping("/me")
  @PreAuthorize("hasAuthority('ROLE_PLATFORM_OWNER')")
  public PlatformIdentityService.Profile me(@AuthenticationPrincipal PlatformPrincipal principal) {
    return service.profile(principal.id());
  }

  public record Credentials(
      @NotBlank @Email @Size(max = 150) String email, @NotBlank @Size(max = 72) String password) {}

  public record Token(String accessToken, String tokenType, long expiresInSeconds) {}
}
