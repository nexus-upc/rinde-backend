package com.nexus.rinde.shared.infrastructure.security;

import com.nexus.rinde.shared.domain.exceptions.AuthenticationFailedException;
import java.util.Optional;
import java.util.UUID;

/**
 * Guarda el usuario autenticado de la petición en curso. Los contextos leen de aquí el tenantId
 * para filtrar siempre los datos por empresa (CRN-04, QA-02).
 */
public final class TenantContext {

  private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

  private TenantContext() {}

  public static void set(CurrentUser user) {
    HOLDER.set(user);
  }

  public static void clear() {
    HOLDER.remove();
  }

  public static Optional<CurrentUser> current() {
    return Optional.ofNullable(HOLDER.get());
  }

  /** Devuelve el usuario actual o falla con 401 si la petición no está autenticada. */
  public static CurrentUser require() {
    return current()
        .orElseThrow(() -> new AuthenticationFailedException("Se requiere iniciar sesión."));
  }

  public static UUID userId() {
    return require().userId();
  }

  public static UUID tenantId() {
    return require().tenantId();
  }

  public static String role() {
    return require().role();
  }

  public static String tenantStatus() {
    return require().tenantStatus();
  }
}
