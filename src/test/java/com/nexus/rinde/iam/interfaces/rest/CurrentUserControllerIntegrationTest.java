package com.nexus.rinde.iam.interfaces.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexus.rinde.iam.domain.model.events.UserInvited;
import com.nexus.rinde.shared.infrastructure.security.JwtClaims;
import com.nexus.rinde.support.AbstractIntegrationTest;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

class CurrentUserControllerIntegrationTest extends AbstractIntegrationTest {

  private static final String PASSWORD = "Clave-Segura-2026";

  @Value("${rinde.security.jwt.secret}")
  private String jwtSecret;

  private String tenantId;
  private String adminToken;

  @BeforeEach
  void registerAdministrator() throws Exception {
    tenantId = api.registerAndVerifyTenant("20123456789", "ana@andes.pe", PASSWORD);
    adminToken = api.signInForToken("ana@andes.pe", PASSWORD);
  }

  @Test
  void returnsTheProfileAndTheTenantOfTheAdministrator() throws Exception {
    api.currentUser(adminToken)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.fullName").value("Administrador 20123456789"))
        .andExpect(jsonPath("$.email").value("ana@andes.pe"))
        .andExpect(jsonPath("$.role").value("ADMINISTRATOR"))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.passwordHash").doesNotExist())
        .andExpect(jsonPath("$.tenant.id").value(tenantId))
        .andExpect(jsonPath("$.tenant.tradeName").value("Empresa 20123456789"))
        .andExpect(jsonPath("$.tenant.ruc").value("20123456789"))
        .andExpect(jsonPath("$.tenant.status").value("ACTIVE"));
  }

  @Test
  void aDriverReadsItsOwnProfileWithTheSameTenant() throws Exception {
    api.inviteUser(adminToken, "Luis Quispe", "luis@andes.pe", "DRIVER")
        .andExpect(status().isCreated());
    String link = capturedEvents.last(UserInvited.class).orElseThrow().invitationToken();
    api.definePassword(link, "Clave-De-Luis-2026").andExpect(status().isNoContent());
    String driverToken = api.signInForToken("luis@andes.pe", "Clave-De-Luis-2026");

    api.currentUser(driverToken)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("luis@andes.pe"))
        .andExpect(jsonPath("$.role").value("DRIVER"))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.tenant.id").value(tenantId));
  }

  @Test
  void requestsWithoutAValidTokenGet401() throws Exception {
    api.currentUser(null).andExpect(status().isUnauthorized());
    api.currentUser("token-invalido").andExpect(status().isUnauthorized());
  }

  @Test
  void aTokenOfAUserThatNoLongerExistsGets404() throws Exception {
    SecretKey key = JwtClaims.signingKey(jwtSecret);
    String token =
        Jwts.builder()
            .subject(UUID.randomUUID().toString())
            .claim(JwtClaims.TENANT_ID, tenantId)
            .claim(JwtClaims.ROLE, "ADMINISTRATOR")
            .claim(JwtClaims.TENANT_STATUS, "ACTIVE")
            .claim(JwtClaims.EMAIL, "fantasma@andes.pe")
            .expiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(key)
            .compact();

    api.currentUser(token)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("El usuario ya no existe."));
  }
}
