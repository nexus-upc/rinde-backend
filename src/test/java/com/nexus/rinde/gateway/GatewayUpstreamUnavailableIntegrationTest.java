package com.nexus.rinde.gateway;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Trip no escucha en su puerto: el gateway debe responder 503 en formato RFC 7807. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayUpstreamUnavailableIntegrationTest {

  private static final String DETAIL = "El servicio no está disponible. Intente nuevamente.";
  private static final int UNUSED_PORT = unusedPort();

  @Autowired WebTestClient webTestClient;

  @DynamicPropertySource
  static void gatewayProperties(DynamicPropertyRegistry registry) {
    registry.add("TRIP_SERVICE_URL", () -> "http://localhost:" + UNUSED_PORT);
    registry.add("JWT_SECRET", () -> AbstractGatewayIntegrationTest.TEST_SECRET);
  }

  @Test
  void answers503WhenTripServiceRefusesConnection() {
    webTestClient
        .get()
        .uri("/api/v1/trips")
        .header(AUTHORIZATION, "Bearer " + AbstractGatewayIntegrationTest.validToken())
        .exchange()
        .expectStatus()
        .isEqualTo(503)
        .expectHeader()
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(503)
        .jsonPath("$.detail")
        .isEqualTo(DETAIL);
  }

  private static int unusedPort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
