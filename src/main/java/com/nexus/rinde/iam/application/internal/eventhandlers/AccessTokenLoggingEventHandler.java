package com.nexus.rinde.iam.application.internal.eventhandlers;

import com.nexus.rinde.iam.domain.model.events.PasswordResetRequested;
import com.nexus.rinde.iam.domain.model.events.TenantRegistered;
import com.nexus.rinde.iam.domain.model.events.UserInvited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Registra los enlaces de acceso: INFO con el token enmascarado, DEBUG con el token completo.
 * TODO: eliminar cuando Notifications envíe los correos con estos eventos.
 */
@Component
public class AccessTokenLoggingEventHandler {

  private static final Logger log = LoggerFactory.getLogger(AccessTokenLoggingEventHandler.class);
  private static final String MASCARA = "****";
  private static final int VISIBLES = 4;

  @EventListener
  public void on(TenantRegistered event) {
    log.info(
        "Token de verificación de la empresa {} para {}: {}",
        event.tenantId(),
        event.administratorEmail(),
        enmascarar(event.verificationToken()));
    log.debug(
        "Token de verificación completo de la empresa {} para {}: {}",
        event.tenantId(),
        event.administratorEmail(),
        event.verificationToken());
  }

  @EventListener
  public void on(UserInvited event) {
    log.info(
        "Token de invitación para {}: {}", event.email(), enmascarar(event.invitationToken()));
    log.debug("Token de invitación completo para {}: {}", event.email(), event.invitationToken());
  }

  @EventListener
  public void on(PasswordResetRequested event) {
    log.info("Token de recuperación para {}: {}", event.email(), enmascarar(event.resetToken()));
    log.debug("Token de recuperación completo para {}: {}", event.email(), event.resetToken());
  }

  /** Deja ver solo los primeros cuatro caracteres; un token corto se oculta por completo. */
  private static String enmascarar(String token) {
    if (token == null) {
      return "null";
    }
    if (token.length() <= VISIBLES) {
      return MASCARA;
    }
    return token.substring(0, VISIBLES) + MASCARA;
  }
}
