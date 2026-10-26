package com.nexus.rinde.trip.infrastructure.fleet;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.nexus.rinde.trip.application.internal.outboundservices.acl.FleetDriverEligibility;
import com.nexus.rinde.trip.application.internal.outboundservices.acl.FleetUnavailableException;
import com.nexus.rinde.trip.application.internal.outboundservices.acl.FleetVehicleHealth;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Cliente HTTP de Fleet contra un WireMock propio: 404, respuestas válidas y fallas de red. */
class FleetHttpClientTest {

  private static final UUID TENANT = UUID.randomUUID();
  private static final UUID VEHICLE = UUID.randomUUID();
  private static final UUID DRIVER = UUID.randomUUID();
  private static final UUID USER = UUID.randomUUID();
  private static final String HEALTH = "/api/internal/v1/fleet/tenants/" + TENANT + "/vehicles/" + VEHICLE + "/health";
  private static final String ELIGIBILITY = "/api/internal/v1/fleet/tenants/" + TENANT + "/drivers/" + DRIVER + "/eligibility";
  private static final String DRIVER_OF_USER = "/api/internal/v1/fleet/tenants/" + TENANT + "/users/" + USER + "/driver";

  private static WireMockServer fleet;
  private FleetHttpClient client;

  @BeforeAll
  static void startFleet() {
    fleet = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    fleet.start();
  }

  @AfterAll
  static void stopFleet() {
    fleet.stop();
  }

  @BeforeEach
  void useFleet() {
    fleet.resetAll();
    client = new FleetHttpClient("http://localhost:" + fleet.port());
  }

  @Test
  void readsTheMaintenanceStateOfAVehicle() {
    fleet.stubFor(
        get(urlPathEqualTo(HEALTH))
            .willReturn(
                okJson(
                    "{\"vehicleId\":\""
                        + VEHICLE
                        + "\",\"maintenanceState\":\"OVERDUE\",\"availableForTrip\":false}")));

    assertThat(client.findVehicleHealth(TENANT, VEHICLE))
        .contains(new FleetVehicleHealth("OVERDUE"));
  }

  @Test
  void readsTheEligibilityOfADriver() {
    fleet.stubFor(
        get(urlPathEqualTo(ELIGIBILITY))
            .willReturn(okJson("{\"driverId\":\"" + DRIVER + "\",\"eligible\":true,\"reason\":\"\"}")));

    assertThat(client.checkDriverEligibility(TENANT, DRIVER))
        .contains(new FleetDriverEligibility(true));
  }

  @Test
  void readsTheDriverLinkedToAUser() {
    fleet.stubFor(
        get(urlPathEqualTo(DRIVER_OF_USER))
            .willReturn(okJson("{\"driverId\":\"" + DRIVER + "\"}")));

    assertThat(client.findDriverIdByUserId(TENANT, USER)).contains(DRIVER);
  }

  @Test
  void aResourceFleetDoesNotKnowIsEmpty() {
    fleet.stubFor(get(urlPathEqualTo(HEALTH)).willReturn(notFound()));
    fleet.stubFor(get(urlPathEqualTo(ELIGIBILITY)).willReturn(notFound()));
    fleet.stubFor(get(urlPathEqualTo(DRIVER_OF_USER)).willReturn(notFound()));

    assertThat(client.findVehicleHealth(TENANT, VEHICLE)).isEmpty();
    assertThat(client.checkDriverEligibility(TENANT, DRIVER)).isEmpty();
    assertThat(client.findDriverIdByUserId(TENANT, USER)).isEmpty();
  }

  @Test
  void aServerErrorFromFleetIsFleetUnavailable() {
    fleet.stubFor(get(urlPathEqualTo(HEALTH)).willReturn(serverError()));

    assertThatThrownBy(() -> client.findVehicleHealth(TENANT, VEHICLE))
        .isInstanceOf(FleetUnavailableException.class);
  }

  @Test
  void aFleetThatCannotBeReachedIsFleetUnavailable() {
    WireMockServer stopped = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    stopped.start();
    int port = stopped.port();
    stopped.stop();
    FleetHttpClient unreachable = new FleetHttpClient("http://localhost:" + port);

    assertThatThrownBy(() -> unreachable.checkDriverEligibility(TENANT, DRIVER))
        .isInstanceOf(FleetUnavailableException.class)
        .hasMessage(
            "El servicio de flota no está disponible en este momento. Intente nuevamente más tarde.");
  }
}
