package com.nexus.rinde.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.*;

import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlatformTokenReaderTest {
  private static final String PLATFORM = "platform-test-key-of-at-least-32-characters";
  private static final String TENANT = "tenant-test-key-of-at-least-32-characters";
  private final PlatformTokenReader reader = new PlatformTokenReader(PLATFORM, TENANT);

  private String token(String key, String audience, String scope, boolean tenant, long seconds) {
    var builder =
        Jwts.builder()
            .subject(UUID.randomUUID().toString())
            .issuer(PlatformTokenReader.ISSUER)
            .audience()
            .add(audience)
            .and()
            .claim("scope", scope)
            .claim("role", "PLATFORM_OWNER")
            .claim("email", "owner@example.invalid")
            .expiration(Date.from(Instant.now().plusSeconds(seconds)));
    if (tenant) builder.claim("tenantId", UUID.randomUUID().toString());
    return builder.signWith(JwtClaims.signingKey(key)).compact();
  }

  @Test
  void acceptsOnlyDedicatedPlatformIdentity() {
    assertThat(reader.read(token(PLATFORM, PlatformTokenReader.AUDIENCE, "PLATFORM", false, 60)))
        .isPresent();
    assertThat(reader.read(token(TENANT, PlatformTokenReader.AUDIENCE, "PLATFORM", false, 60)))
        .isEmpty();
    assertThat(reader.read(token(PLATFORM, "tenant-api", "PLATFORM", false, 60))).isEmpty();
    assertThat(reader.read(token(PLATFORM, PlatformTokenReader.AUDIENCE, "TENANT", false, 60)))
        .isEmpty();
    assertThat(reader.read(token(PLATFORM, PlatformTokenReader.AUDIENCE, "PLATFORM", true, 60)))
        .isEmpty();
    assertThat(reader.read(token(PLATFORM, PlatformTokenReader.AUDIENCE, "PLATFORM", false, -10)))
        .isEmpty();
  }

  @Test
  void cannotReuseTenantSigningKey() {
    assertThatThrownBy(() -> new PlatformTokenReader(TENANT, TENANT))
        .isInstanceOf(IllegalStateException.class);
    assertThat(new PlatformTokenReader("", TENANT).read("invalid")).isEmpty();
  }

  @Test
  void platformTokenNeverBecomesCompanyIdentity() {
    assertThat(
            new JwtTokenReader(TENANT)
                .read(token(PLATFORM, PlatformTokenReader.AUDIENCE, "PLATFORM", false, 60)))
        .isEmpty();
  }
}
