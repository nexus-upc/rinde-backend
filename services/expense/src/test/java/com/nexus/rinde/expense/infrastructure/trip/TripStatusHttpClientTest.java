package com.nexus.rinde.expense.infrastructure.trip;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexus.rinde.support.AbstractIntegrationTest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TripStatusHttpClientTest extends AbstractIntegrationTest {

  @Autowired private TripStatusHttpClient tripClient;

  @Test
  @DisplayName("Consulta HTTP a Trip retorna estado cuando el viaje existe")
  void shouldReturnStatusWhenTripExists() {
    UUID tenantId = UUID.randomUUID();
    UUID tripId = UUID.randomUUID();

    tripStub.setTripStatus(tenantId, tripId, "IN_TRIP");

    Optional<String> status = tripClient.findTripStatus(tenantId, tripId);
    assertThat(status).contains("IN_TRIP");
  }

  @Test
  @DisplayName("Consulta HTTP a Trip retorna vacío cuando el viaje no existe (404)")
  void shouldReturnEmptyWhenTripNotFound() {
    UUID tenantId = UUID.randomUUID();
    UUID tripId = UUID.randomUUID();

    tripStub.setTripNotFound(tenantId, tripId);

    Optional<String> status = tripClient.findTripStatus(tenantId, tripId);
    assertThat(status).isEmpty();
  }
}
