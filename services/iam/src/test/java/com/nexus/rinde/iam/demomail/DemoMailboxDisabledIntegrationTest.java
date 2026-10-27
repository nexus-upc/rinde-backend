package com.nexus.rinde.iam.demomail;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.nexus.rinde.shared.infrastructure.security.JwtClaims;
import com.nexus.rinde.support.AbstractIntegrationTest;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.*;

class DemoMailboxDisabledIntegrationTest extends AbstractIntegrationTest {
  @DynamicPropertySource
  static void disabled(DynamicPropertyRegistry registry) {
    registry.add("rinde.demo.mailbox.enabled", () -> false);
    registry.add("rinde.platform.jwt.secret", () -> "disabled-demo-mailbox-owner-test-key-123456");
  }

  @Test
  void disabledModeHasNoReadableMailboxEvenForAnOwner() throws Exception {
    String owner =
        Jwts.builder()
            .subject(UUID.randomUUID().toString())
            .issuer("rinde-platform")
            .audience()
            .add("rinde-platform-api")
            .and()
            .claim("scope", "PLATFORM")
            .claim("role", "PLATFORM_OWNER")
            .claim("email", "operator@example.invalid")
            .expiration(Date.from(Instant.now().plusSeconds(60)))
            .signWith(JwtClaims.signingKey("disabled-demo-mailbox-owner-test-key-123456"))
            .compact();
    mockMvc
        .perform(get("/api/v1/demo/features"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mailboxEnabled").value(false));
    mockMvc
        .perform(
            get("/api/v1/platform/demo-mailbox/messages")
                .header("Authorization", "Bearer " + owner))
        .andExpect(status().isNotFound());
  }
}
