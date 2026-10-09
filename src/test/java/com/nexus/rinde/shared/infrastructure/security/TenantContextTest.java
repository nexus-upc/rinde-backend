package com.nexus.rinde.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexus.rinde.shared.domain.exceptions.AuthenticationFailedException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantContextTest {

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void exposesTheCurrentUserData() {
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    TenantContext.set(new CurrentUser(userId, tenantId, "ADMINISTRATOR", "ACTIVE", "a@b.com"));

    assertThat(TenantContext.userId()).isEqualTo(userId);
    assertThat(TenantContext.tenantId()).isEqualTo(tenantId);
    assertThat(TenantContext.role()).isEqualTo("ADMINISTRATOR");
    assertThat(TenantContext.tenantStatus()).isEqualTo("ACTIVE");
  }

  @Test
  void failsWithAuthenticationErrorWhenThereIsNoUser() {
    assertThat(TenantContext.current()).isEmpty();
    assertThatThrownBy(TenantContext::tenantId).isInstanceOf(AuthenticationFailedException.class);
  }

  @Test
  void clearRemovesTheUser() {
    TenantContext.set(
        new CurrentUser(UUID.randomUUID(), UUID.randomUUID(), "DRIVER", "ACTIVE", "x@y.com"));

    TenantContext.clear();

    assertThat(TenantContext.current()).isEmpty();
  }
}
