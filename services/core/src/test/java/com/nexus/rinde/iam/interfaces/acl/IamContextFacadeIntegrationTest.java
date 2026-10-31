package com.nexus.rinde.iam.interfaces.acl;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexus.rinde.support.AbstractIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class IamContextFacadeIntegrationTest extends AbstractIntegrationTest {

  private static final String PASSWORD = "Clave-Segura-2026";

  @Autowired private IamContextFacade facade;

  @Test
  void answersWithSimpleDataAndRespectsTheTenant() throws Exception {
    UUID tenantId =
        UUID.fromString(api.registerAndVerifyTenant("20123456789", "ana@andes.pe", PASSWORD));
    String token = api.signInForToken("ana@andes.pe", PASSWORD);
    UUID userId = UUID.fromString(api.json(api.listUsers(token)).get(0).get("id").asText());
    UUID otherTenantId =
        UUID.fromString(api.registerAndVerifyTenant("20987654321", "otro@otra.pe", PASSWORD));

    UserSummary summary = facade.findUserById(tenantId, userId).orElseThrow();

    assertThat(summary.email()).isEqualTo("ana@andes.pe");
    assertThat(summary.role()).isEqualTo("ADMINISTRATOR");
    assertThat(facade.isActiveUserWithRole(tenantId, userId, "ADMINISTRATOR")).isTrue();
    assertThat(facade.findUserById(otherTenantId, userId)).isEmpty();
    assertThat(facade.isActiveUserWithRole(otherTenantId, userId, "ADMINISTRATOR")).isFalse();
    assertThat(facade.findTenantStatus(tenantId)).contains("ACTIVE");
    assertThat(facade.findTenantStatus(UUID.randomUUID())).isEmpty();
  }
}
