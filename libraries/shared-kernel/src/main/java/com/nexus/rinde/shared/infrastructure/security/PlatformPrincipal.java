package com.nexus.rinde.shared.infrastructure.security;

import java.util.UUID;

/** Identidad de plataforma independiente de cualquier empresa. */
public record PlatformPrincipal(UUID id, String email) {
  public static final String AUTHORITY = "ROLE_PLATFORM_OWNER";
}
