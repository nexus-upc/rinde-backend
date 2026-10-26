package com.nexus.rinde.gateway;

import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayRoutingIntegrationTest extends AbstractGatewayIntegrationTest {

  private static final String INTERNAL_STATUS =
      "/api/internal/v1/tenants/00000000-0000-0000-0000-000000000000/status";

  @Test
  void sendsSignInWithoutTokenToIamAndKeepsItsErrorBody() {
    IAM.stubFor(
        post(urlEqualTo("/api/v1/auth/sign-in"))
            .willReturn(
                aResponse()
                    .withStatus(401)
                    .withHeader("Content-Type", "application/problem+json")
                    .withBody("{\"status\":401,\"detail\":\"Credenciales inválidas.\"}")));

    webTestClient
        .post()
        .uri("/api/v1/auth/sign-in")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"email\":\"admin@rinde.test\",\"password\":\"clave-incorrecta\"}")
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Credenciales inválidas.");

    IAM.verify(postRequestedFor(urlEqualTo("/api/v1/auth/sign-in")));
    MONOLITH.verify(0, anyRequestedFor(anyUrl()));
  }

  @Test
  void routesTripsWithValidTokenToMonolithAndForwardsAuthorizationUnchanged() {
    String token = validToken();
    MONOLITH.stubFor(
        get(urlEqualTo("/api/v1/trips/123")).willReturn(okJson("{\"id\":\"123\"}")));

    webTestClient
        .get()
        .uri("/api/v1/trips/123")
        .header(AUTHORIZATION, "Bearer " + token)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.id")
        .isEqualTo("123");

    MONOLITH.verify(
        getRequestedFor(urlEqualTo("/api/v1/trips/123"))
            .withHeader(AUTHORIZATION, equalTo("Bearer " + token)));
    IAM.verify(0, anyRequestedFor(anyUrl()));
  }

  @Test
  void routesUsersAndTenantsToIam() {
    String token = validToken();
    IAM.stubFor(
        get(urlEqualTo("/api/v1/users/42")).willReturn(okJson("{\"id\":\"42\"}")));

    webTestClient
        .get()
        .uri("/api/v1/users/42")
        .header(AUTHORIZATION, "Bearer " + token)
        .exchange()
        .expectStatus()
        .isOk();

    IAM.verify(getRequestedFor(urlEqualTo("/api/v1/users/42")));
    MONOLITH.verify(0, anyRequestedFor(anyUrl()));
  }

  @Test
  void servesOpenApiDocsOfBothServicesThroughRewrittenRoutes() {
    IAM.stubFor(
        get(urlEqualTo("/v3/api-docs")).willReturn(okJson("{\"openapi\":\"3.1.0\"}")));
    MONOLITH.stubFor(
        get(urlEqualTo("/v3/api-docs")).willReturn(okJson("{\"openapi\":\"3.1.0\"}")));

    webTestClient
        .get()
        .uri("/v3/api-docs/iam")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.openapi")
        .exists();
    webTestClient.get().uri("/v3/api-docs/monolith").exchange().expectStatus().isOk();

    IAM.verify(1, getRequestedFor(urlEqualTo("/v3/api-docs")));
    MONOLITH.verify(1, getRequestedFor(urlEqualTo("/v3/api-docs")));
  }

  @Test
  void exposesSwaggerUiWithoutToken() {
    webTestClient.get().uri("/swagger-ui.html").exchange().expectStatus().is3xxRedirection();
  }

  @Test
  void neverForwardsInternalApiAndAnswersNotFound() {
    IAM.stubFor(get(anyUrl()).willReturn(okJson("{}")));

    webTestClient.get().uri(INTERNAL_STATUS).exchange().expectStatus().isNotFound();

    IAM.verify(0, anyRequestedFor(anyUrl()));
    MONOLITH.verify(0, anyRequestedFor(anyUrl()));
  }

  @Test
  void internalApiIsNotFoundEvenWithValidToken() {
    IAM.stubFor(get(anyUrl()).willReturn(okJson("{}")));

    webTestClient
        .get()
        .uri(INTERNAL_STATUS)
        .header(AUTHORIZATION, "Bearer " + validToken())
        .exchange()
        .expectStatus()
        .isNotFound();

    IAM.verify(0, anyRequestedFor(anyUrl()));
  }

  @Test
  void rejectsRelativeSegmentsThatWouldReachInternalApi() {
    IAM.stubFor(get(anyUrl()).willReturn(okJson("{}")));

    webTestClient
        .get()
        .uri(URI.create("/api/v1/%2e%2e/internal/v1/tenants/x/status"))
        .exchange()
        .expectStatus()
        .isBadRequest();
    webTestClient
        .get()
        .uri(URI.create("/api/v1/../internal/v1/tenants/x/status"))
        .exchange()
        .expectStatus()
        .isBadRequest();

    IAM.verify(0, anyRequestedFor(anyUrl()));
    MONOLITH.verify(0, anyRequestedFor(anyUrl()));
  }

  @Test
  void reportsHealthAsUp() {
    webTestClient
        .get()
        .uri("/actuator/health")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo("UP");
  }
}
