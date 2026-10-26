package com.nexus.rinde.iam.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexus.rinde.iam.domain.model.events.UserInvited;
import com.nexus.rinde.support.AbstractIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UsersControllerIntegrationTest extends AbstractIntegrationTest {

  private static final String PASSWORD = "Clave-Segura-2026";

  private String adminToken;

  @BeforeEach
  void registerAdministrator() throws Exception {
    api.registerAndVerifyTenant("20123456789", "ana@andes.pe", PASSWORD);
    adminToken = api.signInForToken("ana@andes.pe", PASSWORD);
  }

  private String inviteDriver(String token, String email) throws Exception {
    JsonNode user =
        api.json(
            api.inviteUser(token, "Luis Quispe", email, "DRIVER").andExpect(status().isCreated()));
    return user.get("id").asText();
  }

  @Test
  void listsOnlyTheUsersOfTheAdministratorsTenant() throws Exception {
    inviteDriver(adminToken, "luis@andes.pe");
    api.registerAndVerifyTenant("20987654321", "otro@otra.pe", PASSWORD);

    api.listUsers(adminToken)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].email").value("ana@andes.pe"))
        .andExpect(jsonPath("$[1].email").value("luis@andes.pe"))
        .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
  }

  @Test
  void invitingCreatesAnInvitedUserAndPublishesTheEvent() throws Exception {
    api.inviteUser(adminToken, "Luis Quispe", "Luis@Andes.pe", "DRIVER")
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.email").value("luis@andes.pe"))
        .andExpect(jsonPath("$.role").value("DRIVER"))
        .andExpect(jsonPath("$.status").value("INVITED"));

    assertThat(capturedEvents.last(UserInvited.class)).isPresent();
  }

  @Test
  void theInvitedUserActivatesWithTheLinkAndCanSignIn() throws Exception {
    inviteDriver(adminToken, "luis@andes.pe");
    String link = capturedEvents.last(UserInvited.class).orElseThrow().invitationToken();
    api.signIn("luis@andes.pe", "Clave-De-Luis-2026").andExpect(status().isUnauthorized());

    api.definePassword(link, "Clave-De-Luis-2026").andExpect(status().isNoContent());

    api.signIn("luis@andes.pe", "Clave-De-Luis-2026").andExpect(status().isOk());
    api.listUsers(adminToken).andExpect(jsonPath("$[1].status").value("ACTIVE"));
  }

  @Test
  void anInvitationLinkCannotBeReused() throws Exception {
    inviteDriver(adminToken, "luis@andes.pe");
    String link = capturedEvents.last(UserInvited.class).orElseThrow().invitationToken();
    api.definePassword(link, "Clave-De-Luis-2026").andExpect(status().isNoContent());

    api.definePassword(link, "Otra-Clave-2026").andExpect(status().isBadRequest());
  }

  @Test
  void invitingAnExistingEmailReturns409() throws Exception {
    inviteDriver(adminToken, "luis@andes.pe");

    api.inviteUser(adminToken, "Otro Luis", "LUIS@andes.pe", "DRIVER")
        .andExpect(status().isConflict());
  }

  @Test
  void invitingWithAnInvalidRoleOrEmailReturns400() throws Exception {
    api.inviteUser(adminToken, "Luis", "luis@andes.pe", "SUPERUSER")
        .andExpect(status().isBadRequest());
    api.inviteUser(adminToken, "Luis", "no-es-correo", "DRIVER")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.email").exists());
  }

  @Test
  void changesTheRoleOfAUser() throws Exception {
    String userId = inviteDriver(adminToken, "luis@andes.pe");

    api.updateUser(adminToken, userId, Map.of("role", "OPERATIONS_MANAGER"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("OPERATIONS_MANAGER"))
        .andExpect(jsonPath("$.status").value("INVITED"));
  }

  @Test
  void disablingAUserBlocksItsSignIn() throws Exception {
    String userId = inviteDriver(adminToken, "luis@andes.pe");
    String link = capturedEvents.last(UserInvited.class).orElseThrow().invitationToken();
    api.definePassword(link, "Clave-De-Luis-2026").andExpect(status().isNoContent());

    api.updateUser(adminToken, userId, Map.of("status", "DISABLED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DISABLED"));

    api.signIn("luis@andes.pe", "Clave-De-Luis-2026").andExpect(status().isForbidden());
  }

  @Test
  void aStatusOtherThanDisabledIsRejected() throws Exception {
    String userId = inviteDriver(adminToken, "luis@andes.pe");

    api.updateUser(adminToken, userId, Map.of("status", "ACTIVE"))
        .andExpect(status().isBadRequest());
    api.updateUser(adminToken, userId, Map.of()).andExpect(status().isBadRequest());
  }

  @Test
  void anAdministratorCannotSeeOrModifyUsersOfAnotherTenant() throws Exception {
    api.registerAndVerifyTenant("20987654321", "otro@otra.pe", PASSWORD);
    String otherAdminToken = api.signInForToken("otro@otra.pe", PASSWORD);
    String foreignUserId = inviteDriver(otherAdminToken, "chofer@otra.pe");

    api.updateUser(adminToken, foreignUserId, Map.of("status", "DISABLED"))
        .andExpect(status().isNotFound());
    api.listUsers(adminToken).andExpect(jsonPath("$", hasSize(1)));
    api.listUsers(otherAdminToken)
        .andExpect(jsonPath("$[?(@.email == 'chofer@otra.pe')].status").value("INVITED"));
  }

  @Test
  void updatingAnUnknownUserReturns404() throws Exception {
    api.updateUser(adminToken, "3f6c1b9e-8a2d-4c57-9a43-0d2f6b1e7a10", Map.of("role", "DRIVER"))
        .andExpect(status().isNotFound());
  }

  @Test
  void aNonAdministratorGets403() throws Exception {
    inviteDriver(adminToken, "luis@andes.pe");
    String link = capturedEvents.last(UserInvited.class).orElseThrow().invitationToken();
    api.definePassword(link, "Clave-De-Luis-2026");
    String driverToken = api.signInForToken("luis@andes.pe", "Clave-De-Luis-2026");

    api.listUsers(driverToken)
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.traceId").exists());
    api.inviteUser(driverToken, "Otro", "otro@andes.pe", "DRIVER")
        .andExpect(status().isForbidden());
  }

  @Test
  void requestsWithoutAValidTokenGet401() throws Exception {
    api.listUsers("token-invalido").andExpect(status().isUnauthorized());
    api.inviteUser(null, "Luis", "luis@andes.pe", "DRIVER").andExpect(status().isUnauthorized());
  }
}
