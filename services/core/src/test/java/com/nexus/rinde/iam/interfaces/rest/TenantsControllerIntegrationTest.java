package com.nexus.rinde.iam.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexus.rinde.iam.domain.model.events.TenantRegistered;
import com.nexus.rinde.support.AbstractIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantsControllerIntegrationTest extends AbstractIntegrationTest {

  private static final String PASSWORD = "Clave-Segura-2026";

  @Test
  void registersATenantPendingVerificationAndPublishesTheEvent() throws Exception {
    api.registerTenant("Transportes Andes", "20123456789", "Ana Rojas", "ana@andes.pe", PASSWORD)
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(header().exists("X-Correlation-Id"))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.tradeName").value("Transportes Andes"))
        .andExpect(jsonPath("$.ruc").value("20123456789"))
        .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"));

    TenantRegistered event = capturedEvents.last(TenantRegistered.class).orElseThrow();
    assertThat(event.administratorEmail()).isEqualTo("ana@andes.pe");
    assertThat(event.verificationToken()).isNotBlank();
  }

  @Test
  void rejectsADuplicatedRucWith409() throws Exception {
    api.registerTenant("Primera", "20123456789", "Ana", "ana@andes.pe", PASSWORD)
        .andExpect(status().isCreated());

    api.registerTenant("Segunda", "20123456789", "Luis", "luis@otra.pe", PASSWORD)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.detail").value("Ya existe una empresa registrada con ese RUC."))
        .andExpect(jsonPath("$.traceId").exists());
  }

  @Test
  void rejectsADuplicatedAdministratorEmailWith409() throws Exception {
    api.registerTenant("Primera", "20123456789", "Ana", "ana@andes.pe", PASSWORD)
        .andExpect(status().isCreated());

    api.registerTenant("Segunda", "20987654321", "Ana", "ANA@andes.pe", PASSWORD)
        .andExpect(status().isConflict());
  }

  @Test
  void rejectsInvalidDataWith400AndTheFieldErrors() throws Exception {
    api.registerTenant("Transportes Andes", "123", "Ana Rojas", "no-es-correo", "corta")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.ruc").exists())
        .andExpect(jsonPath("$.errors.administratorEmail").exists())
        .andExpect(jsonPath("$.errors.password").exists())
        .andExpect(jsonPath("$.traceId").exists());
  }

  @Test
  void verificationActivatesTheTenant() throws Exception {
    String body =
        api.registerTenant("Transportes Andes", "20123456789", "Ana", "ana@andes.pe", PASSWORD)
            .andReturn()
            .getResponse()
            .getContentAsString();
    String tenantId = objectMapper.readTree(body).get("id").asText();
    String token = capturedEvents.last(TenantRegistered.class).orElseThrow().verificationToken();

    api.verifyTenant(tenantId, token)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  void aVerificationLinkCannotBeUsedTwice() throws Exception {
    String body =
        api.registerTenant("Transportes Andes", "20123456789", "Ana", "ana@andes.pe", PASSWORD)
            .andReturn()
            .getResponse()
            .getContentAsString();
    String tenantId = objectMapper.readTree(body).get("id").asText();
    String token = capturedEvents.last(TenantRegistered.class).orElseThrow().verificationToken();
    api.verifyTenant(tenantId, token).andExpect(status().isOk());

    api.verifyTenant(tenantId, token)
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail").value("El enlace no es válido, venció o ya fue utilizado."));
  }

  @Test
  void verificationWithAWrongTokenReturns400() throws Exception {
    String body =
        api.registerTenant("Transportes Andes", "20123456789", "Ana", "ana@andes.pe", PASSWORD)
            .andReturn()
            .getResponse()
            .getContentAsString();
    String tenantId = objectMapper.readTree(body).get("id").asText();

    api.verifyTenant(tenantId, "token-incorrecto").andExpect(status().isBadRequest());
  }

  @Test
  void verificationOfAnUnknownTenantReturns404() throws Exception {
    api.verifyTenant(UUID.randomUUID().toString(), "token").andExpect(status().isNotFound());
  }

  @Test
  void verificationWithAMalformedIdReturns400() throws Exception {
    api.verifyTenant("no-es-uuid", "token").andExpect(status().isBadRequest());
  }
}
