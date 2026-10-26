package com.nexus.rinde.iam.application.internal.eventhandlers;

import com.nexus.rinde.iam.domain.model.events.PasswordResetRequested;
import com.nexus.rinde.iam.domain.model.events.TenantRegistered;
import com.nexus.rinde.iam.domain.model.events.UserInvited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Registra en el log (INFO) los enlaces de acceso, porque todavía no existe el servicio de correo.
 * TODO: eliminar cuando Notifications envíe los correos con estos eventos.
 */
@Component
public class AccessTokenLoggingEventHandler {

  private static final Logger log = LoggerFactory.getLogger(AccessTokenLoggingEventHandler.class);

  @EventListener
  public void on(TenantRegistered event) {
    log.info(
        "Token de verificación de la empresa {} para {}: {}",
        event.tenantId(),
        event.administratorEmail(),
        event.verificationToken());
  }

  @EventListener
  public void on(UserInvited event) {
    log.info("Token de invitación para {}: {}", event.email(), event.invitationToken());
  }

  @EventListener
  public void on(PasswordResetRequested event) {
    log.info("Token de recuperación para {}: {}", event.email(), event.resetToken());
  }
}
