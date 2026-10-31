package com.nexus.rinde.trip.infrastructure.iam;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Estado de la empresa que consulta Trip a IAM, con respaldo cuando IAM no responde. */
class IamTenantStatusClientTest {

  private static final UUID TENANT = UUID.randomUUID();
  private static final String STATUS = "/api/internal/v1/tenants/" + TENANT + "/status";

  private static WireMockServer iam;
  private IamTenantStatusClient client;

  @BeforeAll
  static void startIam() {
    iam = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    iam.start();
  }

  @AfterAll
  static void stopIam() {
    iam.stop();
  }

  @BeforeEach
  void useIam() {
    iam.resetAll();
    client = new IamTenantStatusClient("http://localhost:" + iam.port());
  }

  @Test
  void returnsTheCurrentStatusOfTheTenant() {
    iam.stubFor(
        get(urlPathEqualTo(STATUS))
            .willReturn(okJson("{\"tenantId\":\"" + TENANT + "\",\"status\":\"RESTRICTED\"}")));

    assertThat(client.findTenantStatus(TENANT)).contains("RESTRICTED");
  }

  @Test
  void anUnknownTenantCannotUseTheCachedToken() {
    iam.stubFor(get(urlPathEqualTo(STATUS)).willReturn(notFound()));

    assertThatThrownBy(() -> client.findTenantStatus(TENANT))
        .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
  }

  @Test
  void anUnreachableIamDeniesInsteadOfTrustingStaleTenantStatus() {
    WireMockServer stopped = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    stopped.start();
    int port = stopped.port();
    stopped.stop();

    assertThatThrownBy(
            () -> new IamTenantStatusClient("http://localhost:" + port).findTenantStatus(TENANT))
        .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
  }
}
