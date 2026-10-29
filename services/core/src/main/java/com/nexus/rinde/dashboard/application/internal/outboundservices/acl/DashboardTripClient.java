package com.nexus.rinde.dashboard.application.internal.outboundservices.acl;

import com.nexus.rinde.trip.interfaces.acl.TripSummary;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Obtiene viajes del servicio Trip por HTTP. Reemplaza TripContextFacade en Dashboard. */
@Service
public class DashboardTripClient {

  private static final ParameterizedTypeReference<List<TripResponse>> LIST_TYPE =
      new ParameterizedTypeReference<>() {};

  private final RestClient client;

  public DashboardTripClient(@Qualifier("tripRestClient") RestClient client) {
    this.client = client;
  }

  public Optional<TripSummary> findByIdAndTenantId(UUID tripId, UUID tenantId) {
    try {
      TripResponse body =
          client
              .get()
              .uri("/api/internal/v1/trips/tenants/{tenantId}/{tripId}", tenantId, tripId)
              .retrieve()
              .body(TripResponse.class);
      return Optional.ofNullable(body).map(DashboardTripClient::toSummary);
    } catch (RestClientResponseException ex) {
      if (ex.getStatusCode().value() == 404) return Optional.empty();
      throw new DashboardTripUnavailableException(ex);
    } catch (RestClientException ex) {
      throw new DashboardTripUnavailableException(ex);
    }
  }

  public List<TripSummary> findByTenantId(UUID tenantId) {
    try {
      List<TripResponse> body =
          client
              .get()
              .uri("/api/internal/v1/trips/tenants/{tenantId}", tenantId)
              .retrieve()
              .body(LIST_TYPE);
      if (body == null) return List.of();
      return body.stream().map(DashboardTripClient::toSummary).toList();
    } catch (RestClientException ex) {
      throw new DashboardTripUnavailableException(ex);
    }
  }

  private static TripSummary toSummary(TripResponse r) {
    return new TripSummary(
        r.tripId(),
        r.code(),
        r.status(),
        r.origin(),
        r.destination(),
        r.cargoDescription(),
        r.cargoWeightKg(),
        r.departureDate(),
        r.vehicleId(),
        r.driverId(),
        r.startedAt(),
        r.finishedAt());
  }

  public record TripResponse(
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
      Instant finishedAt) {}
}
