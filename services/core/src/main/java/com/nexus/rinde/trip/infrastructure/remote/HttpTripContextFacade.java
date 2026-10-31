package com.nexus.rinde.trip.infrastructure.remote;

import com.nexus.rinde.configuration.remote.RemoteServiceUnavailableException;
import com.nexus.rinde.trip.interfaces.acl.TripContextFacade;
import com.nexus.rinde.trip.interfaces.acl.TripSummary;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Consulta los viajes en el servicio Trip separado; reemplaza la copia antigua de core. */
@Service
@Primary
@ConditionalOnProperty(name = "rinde.trip.remote-enabled", havingValue = "true")
public class HttpTripContextFacade implements TripContextFacade {

  private static final ParameterizedTypeReference<List<TripResponse>> LIST =
      new ParameterizedTypeReference<>() {};

  private final RestClient client;

  public HttpTripContextFacade(@Qualifier("tripRestClient") RestClient client) {
    this.client = client;
  }

  @Override
  public Optional<TripSummary> findByIdAndTenantId(UUID tripId, UUID tenantId) {
    try {
      return Optional.ofNullable(
              client
                  .get()
                  .uri("/api/internal/v1/trips/tenants/{tenant}/{trip}", tenantId, tripId)
                  .retrieve()
                  .body(TripResponse.class))
          .map(TripResponse::toSummary);
    } catch (HttpClientErrorException.NotFound ex) {
      return Optional.empty();
    } catch (RestClientException ex) {
      throw new RemoteServiceUnavailableException("viajes", ex);
    }
  }

  @Override
  public List<TripSummary> findByTenantId(UUID tenantId) {
    try {
      List<TripResponse> body =
          client.get().uri("/api/internal/v1/trips/tenants/{tenant}", tenantId).retrieve().body(LIST);
      return body == null ? List.of() : body.stream().map(TripResponse::toSummary).toList();
    } catch (RestClientException ex) {
      throw new RemoteServiceUnavailableException("viajes", ex);
    }
  }

  record TripResponse(
      UUID tripId,
      String code,
      String status,
      String origin,
      String destination,
      String cargoDescription,
      BigDecimal cargoWeightKg,
      LocalDate departureDate,
      UUID vehicleId,
      UUID driverId,
      Instant startedAt,
      Instant finishedAt) {

    TripSummary toSummary() {
      return new TripSummary(
          tripId, code, status, origin, destination, cargoDescription, cargoWeightKg,
          departureDate, vehicleId, driverId, startedAt, finishedAt);
    }
  }
}
