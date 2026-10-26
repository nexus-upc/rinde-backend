package com.nexus.rinde.trip.infrastructure.fleet;

import com.nexus.rinde.trip.application.internal.outboundservices.acl.FleetContextClient;
import com.nexus.rinde.trip.application.internal.outboundservices.acl.FleetDriverEligibility;
import com.nexus.rinde.trip.application.internal.outboundservices.acl.FleetUnavailableException;
import com.nexus.rinde.trip.application.internal.outboundservices.acl.FleetVehicleHealth;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente HTTP de las consultas internas de Fleet. Un 404 significa que el recurso no existe en la
 * empresa. Cualquier otra falla, incluida una caída de la conexión, se informa como 503.
 */
@Component
public class FleetHttpClient implements FleetContextClient {

  private static final Logger log = LoggerFactory.getLogger(FleetHttpClient.class);
  private static final String HEALTH_PATH =
      "/api/internal/v1/fleet/tenants/{tenantId}/vehicles/{vehicleId}/health";
  private static final String ELIGIBILITY_PATH =
      "/api/internal/v1/fleet/tenants/{tenantId}/drivers/{driverId}/eligibility";
  private static final String DRIVER_PATH =
      "/api/internal/v1/fleet/tenants/{tenantId}/users/{userId}/driver";

  private final RestClient restClient;

  public FleetHttpClient(@Value("${rinde.fleet.service-url}") String serviceUrl) {
    ClientHttpRequestFactory requestFactory =
        ClientHttpRequestFactories.get(
            ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(2))
                .withReadTimeout(Duration.ofSeconds(5)));
    this.restClient = RestClient.builder().baseUrl(serviceUrl).requestFactory(requestFactory).build();
  }

  @Override
  public Optional<FleetVehicleHealth> findVehicleHealth(UUID tenantId, UUID vehicleId) {
    return get(HEALTH_PATH, VehicleHealthResponse.class, tenantId, vehicleId)
        .map(response -> new FleetVehicleHealth(response.maintenanceState()));
  }

  @Override
  public Optional<FleetDriverEligibility> checkDriverEligibility(UUID tenantId, UUID driverId) {
    return get(ELIGIBILITY_PATH, DriverEligibilityResponse.class, tenantId, driverId)
        .map(response -> new FleetDriverEligibility(response.eligible()));
  }

  @Override
  public Optional<UUID> findDriverIdByUserId(UUID tenantId, UUID userId) {
    return get(DRIVER_PATH, DriverResponse.class, tenantId, userId)
        .map(DriverResponse::driverId);
  }

  private <T> Optional<T> get(String path, Class<T> type, Object... variables) {
    try {
      return Optional.ofNullable(restClient.get().uri(path, variables).retrieve().body(type));
    } catch (HttpClientErrorException.NotFound ex) {
      return Optional.empty();
    } catch (RestClientException ex) {
      log.warn("Fleet no respondió correctamente a {}: {}", path, ex.getMessage());
      throw new FleetUnavailableException();
    }
  }

  /** Cuerpo de GET .../health: el identificador de la unidad no lo necesita Trip. */
  public record VehicleHealthResponse(String maintenanceState, boolean availableForTrip) {}

  /** Cuerpo de GET .../eligibility: el motivo se informa a Fleet, Trip solo usa la habilitación. */
  public record DriverEligibilityResponse(boolean eligible, String reason) {}

  /** Cuerpo de GET .../driver: identificador del conductor vinculado al usuario. */
  public record DriverResponse(UUID driverId) {}
}
