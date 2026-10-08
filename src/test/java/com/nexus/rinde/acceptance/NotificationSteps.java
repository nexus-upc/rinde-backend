package com.nexus.rinde.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexus.rinde.notification.infrastructure.persistence.jpa.entities.RetryStoreEntry;
import com.nexus.rinde.notification.infrastructure.persistence.jpa.repositories.RetryStoreRepository;
import com.nexus.rinde.support.CapturedEvents;
import com.nexus.rinde.trip.domain.model.events.TripAssigned;
import io.cucumber.java.es.Y;
import java.time.LocalDate;

/** Pasos BDD que verifican recepción del evento y preparación del aviso pendiente. */
public class NotificationSteps {

  private final ScenarioContext context;
  private final CapturedEvents capturedEvents;
  private final RetryStoreRepository retryStoreRepository;

  public NotificationSteps(
      ScenarioContext context,
      CapturedEvents capturedEvents,
      RetryStoreRepository retryStoreRepository) {
    this.context = context;
    this.capturedEvents = capturedEvents;
    this.retryStoreRepository = retryStoreRepository;
  }

  @Y(
      "Notifications prepara un aviso pendiente para el conductor {string} con el código del viaje,"
          + " el destino {string} y la fecha de salida {string}")
  public void notificationsPreparesPendingNotice(
      String driverEmail, String destination, String departureDate) {
    TripAssigned event = capturedEvents.last(TripAssigned.class).orElseThrow();
    RetryStoreEntry entry = retryStoreRepository.findById(event.eventId()).orElseThrow();

    assertThat(context.emailOfUserId(event.driverId().toString())).isEqualTo(driverEmail);
    assertThat(entry.getTenantId()).isEqualTo(event.tenantId());
    assertThat(entry.getRecipientDriverId()).isEqualTo(event.driverId());
    assertThat(entry.getTripCode()).isEqualTo(event.code());
    assertThat(entry.getDestination()).isEqualTo(destination);
    assertThat(entry.getDepartureDate()).isEqualTo(LocalDate.parse(departureDate));
    assertThat(entry.getMessage())
        .contains(event.code(), destination, departureDate);
    assertThat(entry.getDeliveryStatus()).isEqualTo("PENDING");
    assertThat(entry.getRetryCount()).isZero();
    assertThat(entry.getLastError()).containsIgnoringCase("no hay token de dispositivo");
  }
}
