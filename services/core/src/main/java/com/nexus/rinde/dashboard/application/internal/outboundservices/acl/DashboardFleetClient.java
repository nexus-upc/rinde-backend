package com.nexus.rinde.dashboard.application.internal.outboundservices.acl;

import com.nexus.rinde.fleet.interfaces.acl.FleetDriverSummary;
import com.nexus.rinde.fleet.interfaces.acl.FleetVehicleSummary;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Obtiene vehículos y conductores del servicio Fleet por HTTP. Reemplaza FleetContextFacade en Dashboard. */
@Service
public class DashboardFleetClient {

  private static final ParameterizedTypeReference<List<VehicleResponse>> VEHICLE_LIST =
      new ParameterizedTypeReference<>() {};

  private static final ParameterizedTypeReference<List<DriverResponse>> DRIVER_LIST =
      new ParameterizedTypeReference<>() {};

  private final RestClient client;

  public DashboardFleetClient(@Qualifier("dashboardFleetRestClient") RestClient client) {
    this.client = client;
  }

  public List<FleetVehicleSummary> findVehiclesByTenantId(UUID tenantId) {
    try {
      List<VehicleResponse> body =
          client
              .get()
              .uri("/api/internal/v1/fleet/tenants/{tenantId}/vehicles", tenantId)
              .retrieve()
              .body(VEHICLE_LIST);
      if (body == null) return List.of();
      return body.stream()
          .map(v -> new FleetVehicleSummary(v.id(), v.plateNumber(), v.brand(), v.model(), v.status()))
          .toList();
    } catch (RestClientException ex) {
      throw new DashboardFleetUnavailableException(ex);
    }
  }

  public List<FleetDriverSummary> findDriversByTenantId(UUID tenantId) {
    try {
      List<DriverResponse> body =
          client
              .get()
              .uri("/api/internal/v1/fleet/tenants/{tenantId}/drivers", tenantId)
              .retrieve()
              .body(DRIVER_LIST);
      if (body == null) return List.of();
      return body.stream()
          .map(d -> new FleetDriverSummary(d.id(), d.fullName(), d.licenseNumber(), d.licenseCategory(), d.status()))
          .toList();
    } catch (RestClientException ex) {
      throw new DashboardFleetUnavailableException(ex);
    }
  }

  public record VehicleResponse(UUID id, String plateNumber, String brand, String model, String status) {}

  public record DriverResponse(UUID id, String fullName, String licenseNumber, String licenseCategory, String status) {}
}
