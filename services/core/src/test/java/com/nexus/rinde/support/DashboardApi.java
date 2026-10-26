package com.nexus.rinde.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Cliente de pruebas para los endpoints de Dashboard. */
public class DashboardApi {

  private final MockMvc mockMvc;

  public DashboardApi(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  public ResultActions getTripSummary(String token, String tripId) throws Exception {
    return mockMvc.perform(
        authorized(get("/api/v1/dashboard/trips/" + tripId + "/summary"), token));
  }

  public ResultActions getFleetStatus(String token) throws Exception {
    return mockMvc.perform(authorized(get("/api/v1/dashboard/fleet-status"), token));
  }

  public ResultActions getMetrics(String token) throws Exception {
    return mockMvc.perform(authorized(get("/api/v1/dashboard/metrics"), token));
  }

  private MockHttpServletRequestBuilder authorized(
      MockHttpServletRequestBuilder request, String token) {
    return token == null ? request : request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
  }
}
