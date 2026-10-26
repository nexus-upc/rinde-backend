package com.nexus.rinde.iam.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.domain.model.valueobjects.AuthenticationResult;
import com.nexus.rinde.iam.domain.model.valueobjects.Email;
import com.nexus.rinde.iam.domain.model.valueobjects.PasswordHash;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantStatus;
import com.nexus.rinde.shared.infrastructure.security.CurrentUser;
import com.nexus.rinde.shared.infrastructure.security.JwtTokenReader;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

  private static final String SECRET = "test-secret-for-unit-tests-with-32-chars!!";

  private final User user =
      User.createAdministrator(
          new TenantId(UUID.randomUUID()),
          "Ana Rojas",
          new Email("ana@andes.pe"),
          new PasswordHash("$2a$10$hash"));

  @Test
  void issuesATokenThatTheSharedReaderCanRead() {
    JwtTokenProvider provider =
        new JwtTokenProvider(new JwtProperties(SECRET, 8), Clock.systemUTC());

    AuthenticationResult result = provider.issueFor(user, TenantStatus.RESTRICTED);
    CurrentUser current = new JwtTokenReader(SECRET).read(result.accessToken()).orElseThrow();

    assertThat(result.expiresInSeconds()).isEqualTo(8 * 3600);
    assertThat(current.userId()).isEqualTo(user.getId());
    assertThat(current.tenantId()).isEqualTo(user.getTenantId().value());
    assertThat(current.role()).isEqualTo("ADMINISTRATOR");
    assertThat(current.tenantStatus()).isEqualTo("RESTRICTED");
    assertThat(current.email()).isEqualTo("ana@andes.pe");
  }

  @Test
  void theTokenExpiresAfterTheConfiguredHours() {
    Clock past = Clock.fixed(Instant.now().minusSeconds(3 * 3600), ZoneOffset.UTC);
    JwtTokenProvider provider = new JwtTokenProvider(new JwtProperties(SECRET, 2), past);

    AuthenticationResult result = provider.issueFor(user, TenantStatus.ACTIVE);

    assertThat(new JwtTokenReader(SECRET).read(result.accessToken())).isEmpty();
  }

  @Test
  void refusesToStartWithAShortSecret() {
    assertThatThrownBy(() -> new JwtTokenProvider(new JwtProperties("short", 8), Clock.systemUTC()))
        .isInstanceOf(IllegalStateException.class);
  }
}
