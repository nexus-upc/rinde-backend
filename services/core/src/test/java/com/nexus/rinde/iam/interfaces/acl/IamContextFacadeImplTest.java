package com.nexus.rinde.iam.interfaces.acl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nexus.rinde.iam.domain.model.aggregates.Tenant;
import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.domain.model.queries.GetTenantByIdQuery;
import com.nexus.rinde.iam.domain.model.queries.GetUserByIdQuery;
import com.nexus.rinde.iam.domain.model.valueobjects.AccessToken;
import com.nexus.rinde.iam.domain.model.valueobjects.Email;
import com.nexus.rinde.iam.domain.model.valueobjects.PasswordHash;
import com.nexus.rinde.iam.domain.model.valueobjects.Role;
import com.nexus.rinde.iam.domain.model.valueobjects.Ruc;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.model.valueobjects.TokenPurpose;
import com.nexus.rinde.iam.domain.services.TenantQueryService;
import com.nexus.rinde.iam.domain.services.UserQueryService;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IamContextFacadeImplTest {

  private static final Instant NOW = Instant.parse("2026-10-06T10:00:00Z");

  @Mock private UserQueryService userQueryService;
  @Mock private TenantQueryService tenantQueryService;

  private IamContextFacadeImpl facade;
  private final TenantId tenantId = new TenantId(UUID.randomUUID());

  @BeforeEach
  void setUp() {
    facade = new IamContextFacadeImpl(userQueryService, tenantQueryService);
  }

  private User activeUser() {
    return User.createAdministrator(
        tenantId, "Ana Rojas", new Email("ana@andes.pe"), new PasswordHash("hash"));
  }

  private User invitedDriver() {
    User user = new User(tenantId, "Luis Quispe", new Email("luis@andes.pe"), Role.DRIVER);
    user.invite(Duration.ofHours(48), NOW);
    return user;
  }

  @Test
  void findsAUserAsSimpleData() {
    User user = activeUser();
    when(userQueryService.handle(new GetUserByIdQuery(tenantId, user.getUserId())))
        .thenReturn(Optional.of(user));

    Optional<UserSummary> summary = facade.findUserById(tenantId.value(), user.getId());

    assertThat(summary)
        .contains(
            new UserSummary(
                user.getId(),
                tenantId.value(),
                "Ana Rojas",
                "ana@andes.pe",
                "ADMINISTRATOR",
                "ACTIVE"));
  }

  @Test
  void doesNotFindAUserOutsideTheTenant() {
    when(userQueryService.handle(any(GetUserByIdQuery.class))).thenReturn(Optional.empty());

    assertThat(facade.findUserById(tenantId.value(), UUID.randomUUID())).isEmpty();
  }

  @Test
  void confirmsAnActiveUserWithTheRole() {
    User user = activeUser();
    when(userQueryService.handle(any(GetUserByIdQuery.class))).thenReturn(Optional.of(user));

    assertThat(facade.isActiveUserWithRole(tenantId.value(), user.getId(), "ADMINISTRATOR"))
        .isTrue();
    assertThat(facade.isActiveUserWithRole(tenantId.value(), user.getId(), "DRIVER")).isFalse();
  }

  @Test
  void anInvitedUserIsNotActive() {
    User user = invitedDriver();
    when(userQueryService.handle(any(GetUserByIdQuery.class))).thenReturn(Optional.of(user));

    assertThat(facade.isActiveUserWithRole(tenantId.value(), user.getId(), "DRIVER")).isFalse();
  }

  @Test
  void aMissingUserDoesNotHaveTheRole() {
    when(userQueryService.handle(any(GetUserByIdQuery.class))).thenReturn(Optional.empty());

    assertThat(facade.isActiveUserWithRole(tenantId.value(), UUID.randomUUID(), "DRIVER"))
        .isFalse();
  }

  @Test
  void returnsTheTenantStatusAsText() {
    Tenant tenant = new Tenant("Transportes Andes", new Ruc("20123456789"));
    tenant.register(
        new Email("ana@andes.pe"),
        AccessToken.issue(
            tenant.getTenantId(), TokenPurpose.EMAIL_VERIFICATION, Duration.ofHours(48), NOW),
        NOW);
    tenant.verify();
    when(tenantQueryService.handle(new GetTenantByIdQuery(tenant.getTenantId())))
        .thenReturn(Optional.of(tenant));

    assertThat(facade.findTenantStatus(tenant.getId())).contains("ACTIVE");
  }

  @Test
  void anUnknownTenantHasNoStatus() {
    when(tenantQueryService.handle(any(GetTenantByIdQuery.class))).thenReturn(Optional.empty());

    assertThat(facade.findTenantStatus(UUID.randomUUID())).isEmpty();
  }
}
