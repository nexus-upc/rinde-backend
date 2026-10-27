package com.nexus.rinde.iam.platform;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.TenantRepository;
import com.nexus.rinde.iam.platform.domain.PlatformOwner;
import com.nexus.rinde.iam.platform.infrastructure.PlatformOwnerRepository;
import com.nexus.rinde.support.AbstractIntegrationTest;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class PlatformIdentityIntegrationTest extends AbstractIntegrationTest {
  @Autowired PlatformOwnerRepository owners;
  @Autowired PasswordEncoder encoder;
  @Autowired TenantRepository tenants;
  private static final String OWNER_PASSWORD = "TestOwnerPassword!42";

  @DynamicPropertySource
  static void platformProperties(DynamicPropertyRegistry registry) {
    registry.add("rinde.platform.jwt.secret", () -> "separate-platform-integration-secret-12345");
    registry.add("rinde.platform.bootstrap.enabled", () -> false);
  }

  @BeforeEach
  void ownerFixture() {
    owners.save(
        new PlatformOwner(
            "Dueño de prueba",
            "owner@example.invalid",
            encoder.encode(OWNER_PASSWORD),
            Instant.now()));
  }

  private String ownerToken() throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/platform/auth/sign-in")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of("email", "owner@example.invalid", "password", OWNER_PASSWORD))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(body).get("accessToken").asText();
  }

  @Test
  void ownerHasNoTenantAndCannotEnterCompanyRoutes() throws Exception {
    String token = ownerToken();
    mockMvc
        .perform(get("/api/v1/platform/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("PLATFORM_OWNER"));
    mockMvc
        .perform(get("/api/v1/users").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
    assertThat(tenants.count()).isZero();
  }

  @Test
  void companyCannotObtainOrAssignThePlatformRole() throws Exception {
    api.registerAndVerifyTenant("20123456789", "company@example.invalid", "CompanyPassword!42");
    String token = api.signInForToken("company@example.invalid", "CompanyPassword!42");
    mockMvc
        .perform(get("/api/v1/platform/companies").header("Authorization", "Bearer " + token))
        .andExpect(status().is4xxClientError());
    mockMvc
        .perform(
            post("/api/v1/platform/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "email", "company@example.invalid", "password", "CompanyPassword!42"))))
        .andExpect(status().isUnauthorized());
    mockMvc
        .perform(
            post("/api/v1/users")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"fullName\":\"Intruso\",\"email\":\"intruder@example.invalid\",\"role\":\"PLATFORM_OWNER\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void suspensionBlocksExistingTokensAndRestorePreservesBillingRestriction() throws Exception {
    String id =
        api.registerAndVerifyTenant("20123456789", "company@example.invalid", "CompanyPassword!42");
    String company = api.signInForToken("company@example.invalid", "CompanyPassword!42");
    String owner = ownerToken();
    mockMvc
        .perform(
            patch("/api/v1/platform/companies/" + id + "/access")
                .header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"suspended\":true,\"reason\":\"Solicitud de soporte\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessSuspended").value(true));
    mockMvc
        .perform(get("/api/v1/users").header("Authorization", "Bearer " + company))
        .andExpect(status().isForbidden());
    var tenant = tenants.findById(java.util.UUID.fromString(id)).orElseThrow();
    tenant.restrict();
    tenants.saveAndFlush(tenant);
    mockMvc
        .perform(
            patch("/api/v1/platform/companies/" + id + "/access")
                .header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"suspended\":false,\"reason\":\"Revisión finalizada\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("RESTRICTED"));
    mockMvc
        .perform(get("/api/v1/platform/audit/identity").header("Authorization", "Bearer " + owner))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(2));
  }

  @Test
  void paginatedSearchAndValidationUseRealCompanyData() throws Exception {
    api.registerAndVerifyTenant("20123456789", "company@example.invalid", "CompanyPassword!42");
    String owner = ownerToken();
    mockMvc
        .perform(
            get("/api/v1/platform/companies")
                .param("search", "20123456789")
                .header("Authorization", "Bearer " + owner))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(1));
    mockMvc
        .perform(
            get("/api/v1/platform/companies")
                .param("size", "101")
                .header("Authorization", "Bearer " + owner))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(get("/api/v1/platform/overview").header("Authorization", "Bearer " + owner))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.companies").value(1));
  }
}
