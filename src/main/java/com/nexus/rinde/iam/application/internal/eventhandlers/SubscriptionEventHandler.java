package com.nexus.rinde.iam.application.internal.eventhandlers;

import com.nexus.rinde.iam.domain.model.commands.ReactivateTenantCommand;
import com.nexus.rinde.iam.domain.model.commands.RestrictTenantCommand;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.services.TenantCommandService;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import com.nexus.rinde.subscription.interfaces.acl.SubscriptionActivated;
import com.nexus.rinde.subscription.interfaces.acl.SubscriptionSuspended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reacciona a los eventos de Subscriptions & Billing: reactiva o restringe la empresa. Los comandos
 * son idempotentes, así que un evento repetido no cambia el resultado (CRN-08).
 */
@Component
public class SubscriptionEventHandler {

  private static final Logger log = LoggerFactory.getLogger(SubscriptionEventHandler.class);

  private final TenantCommandService tenantCommandService;

  public SubscriptionEventHandler(TenantCommandService tenantCommandService) {
    this.tenantCommandService = tenantCommandService;
  }

  @EventListener
  public void on(SubscriptionActivated event) {
    try {
      tenantCommandService.handle(new ReactivateTenantCommand(new TenantId(event.tenantId())));
    } catch (ResourceNotFoundException ex) {
      log.warn("SubscriptionActivated para una empresa inexistente: {}", event.tenantId());
    }
  }

  @EventListener
  public void on(SubscriptionSuspended event) {
    try {
      tenantCommandService.handle(new RestrictTenantCommand(new TenantId(event.tenantId())));
    } catch (ResourceNotFoundException ex) {
      log.warn("SubscriptionSuspended para una empresa inexistente: {}", event.tenantId());
    }
  }
}
