package com.nexus.rinde.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base de las pruebas de integración: contexto completo contra rinde_trip_test, con Fleet e IAM
 * simulados por WireMock y datos limpios en cada prueba.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

  @Autowired protected TestDatabase testDatabase;
  @Autowired protected CapturedEvents capturedEvents;
  @Autowired protected FleetStub fleetStub;
  @Autowired protected TestTokens tokens;
  @Autowired protected MockMvc mockMvc;
  @Autowired protected ObjectMapper objectMapper;

  protected TripApi api;

  @DynamicPropertySource
  static void externalProperties(DynamicPropertyRegistry registry) {
    TestDatabase.registerUrl(registry);
    registry.add("rinde.fleet.service-url", WireMockServices::baseUrl);
    registry.add("rinde.iam.service-url", WireMockServices::baseUrl);
  }

  @BeforeEach
  void cleanData() {
    testDatabase.clean();
    capturedEvents.clear();
    fleetStub.reset();
    api = new TripApi(mockMvc, objectMapper);
  }
}
