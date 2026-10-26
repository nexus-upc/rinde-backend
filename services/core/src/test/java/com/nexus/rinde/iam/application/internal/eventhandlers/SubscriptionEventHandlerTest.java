package com.nexus.rinde.iam.application.internal.eventhandlers;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.nexus.rinde.iam.domain.model.commands.ReactivateTenantCommand;
import com.nexus.rinde.iam.domain.model.commands.RestrictTenantCommand;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.services.TenantCommandService;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import com.nexus.rinde.subscription.interfaces.acl.SubscriptionActivated;
import com.nexus.rinde.subscription.interfaces.acl.SubscriptionSuspended;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionEventHandlerTest {

  @Mock private TenantCommandService tenantCommandService;
  @InjectMocks private SubscriptionEventHandler handler;

  private final UUID tenantId = UUID.randomUUID();

  @Test
  void subscriptionActivatedReactivatesTheTenant() {
    handler.on(new SubscriptionActivated(UUID.randomUUID(), Instant.now(), tenantId));

    verify(tenantCommandService).handle(new ReactivateTenantCommand(new TenantId(tenantId)));
  }

  @Test
  void subscriptionSuspendedRestrictsTheTenant() {
    handler.on(new SubscriptionSuspended(UUID.randomUUID(), Instant.now(), tenantId));

    verify(tenantCommandService).handle(new RestrictTenantCommand(new TenantId(tenantId)));
  }

  @Test
  void anUnknownTenantDoesNotBreakThePublisher() {
    doThrow(new ResourceNotFoundException("no existe"))
        .when(tenantCommandService)
        .handle(any(RestrictTenantCommand.class));
    doThrow(new ResourceNotFoundException("no existe"))
        .when(tenantCommandService)
        .handle(any(ReactivateTenantCommand.class));

    assertThatCode(
            () -> {
              handler.on(new SubscriptionSuspended(UUID.randomUUID(), Instant.now(), tenantId));
              handler.on(new SubscriptionActivated(UUID.randomUUID(), Instant.now(), tenantId));
            })
        .doesNotThrowAnyException();
  }
}
