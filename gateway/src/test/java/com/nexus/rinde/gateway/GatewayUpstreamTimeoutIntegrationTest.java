package com.nexus.rinde.gateway;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Trip responde, pero tarda más que el tiempo de espera del gateway: debe responder 503. */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.cloud.gateway.server.webflux.httpclient.response-timeout=300ms")
@AutoConfigureWebTestClient
class GatewayUpstreamTimeoutIntegrationTest {

  private static final String DETAIL = "El servicio no está disponible. Intente nuevamente.";
  private static final WireMockServer SLOW_TRIP =
      new WireMockServer(wireMockConfig().dynamicPort());

  static {
    SLOW_TRIP.start();
  }

  @Autowired WebTestClient webTestClient;

  @DynamicPropertySource
  static void gatewayProperties(DynamicPropertyRegistry registry) {
    registry.add("TRIP_SERVICE_URL", () -> "http://localhost:" + SLOW_TRIP.port());
    registry.add("JWT_SECRET", () -> AbstractGatewayIntegrationTest.TEST_SECRET);
  }

  @AfterAll
  static void stopSlowTrip() {
    SLOW_TRIP.stop();
  }

  @Test
  void answers503WhenTripServiceDoesNotAnswerInTime() {
    SLOW_TRIP.stubFor(
        get(urlEqualTo("/api/v1/trips"))
            .willReturn(okJson("[]").withFixedDelay(2000)));

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
        .jsonPath("$.detail")
        .isEqualTo(DETAIL);
  }
}
