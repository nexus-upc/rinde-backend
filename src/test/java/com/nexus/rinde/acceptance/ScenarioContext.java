package com.nexus.rinde.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.spring.ScenarioScope;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

/** Estado que comparten los pasos de un mismo escenario: última respuesta, tokens e ids creados. */
@Component
@ScenarioScope
public class ScenarioContext {

  public static final String PASSWORD = "Clave-Segura-2026";

  private final ObjectMapper objectMapper;
  private final Map<String, String> accessTokens = new HashMap<>();
  private final Map<String, String> tenantIds = new HashMap<>();
  private final Map<String, String> userIds = new HashMap<>();
  private final Map<String, String> tripIds = new HashMap<>();
  private final Map<String, String> vehicleIds = new HashMap<>();
  private MvcResult lastResult;
  private String currentAdministratorToken;

  public ScenarioContext(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /** Guarda la respuesta del último request para que los pasos "Entonces" la revisen. */
  public ResultActions record(ResultActions result) {
    this.lastResult = result.andReturn();
    return result;
  }

  public int statusCode() {
    return lastResult.getResponse().getStatus();
  }

  public JsonNode body() throws Exception {
    return objectMapper.readTree(lastResult.getResponse().getContentAsString());
  }

  public void rememberAccessToken(String email, String token) {
    accessTokens.put(email, token);
  }

  public String accessTokenOf(String email) {
    return accessTokens.get(email);
  }

  public void rememberTenantId(String ruc, String tenantId) {
    tenantIds.put(ruc, tenantId);
  }

  public String tenantIdOf(String ruc) {
    return tenantIds.get(ruc);
  }

  public void rememberUserId(String email, String userId) {
    userIds.put(email, userId);
  }

  public String userIdOf(String email) {
    return userIds.get(email);
  }

  public void rememberTripId(String name, String tripId) {
    tripIds.put(name, tripId);
  }

  public String tripIdOf(String name) {
    return tripIds.get(name);
  }

  public void rememberVehicleId(String name, String vehicleId) {
    vehicleIds.put(name, vehicleId);
  }

  public String vehicleIdOf(String name) {
    return vehicleIds.get(name);
  }

  public String emailOfUserId(String userId) {
    return userIds.entrySet().stream()
        .filter(entry -> entry.getValue().equals(userId))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(null);
  }

  public String currentAdministratorToken() {
    return currentAdministratorToken;
  }

  public void setCurrentAdministratorToken(String token) {
    this.currentAdministratorToken = token;
  }
}
