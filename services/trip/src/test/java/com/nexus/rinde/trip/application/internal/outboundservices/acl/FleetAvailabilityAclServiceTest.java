package com.nexus.rinde.trip.application.internal.outboundservices.acl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import com.nexus.rinde.trip.domain.model.valueobjects.Availability;
import com.nexus.rinde.trip.domain.model.valueobjects.MaintenanceState;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FleetAvailabilityAclServiceTest {

  private static final UUID TENANT = UUID.randomUUID();
  private static final UUID VEHICLE = UUID.randomUUID();
  private static final UUID DRIVER = UUID.randomUUID();
  private static final UUID USER = UUID.randomUUID();

  @Mock private FleetContextClient fleetContextClient;

  @Test
  void translatesFleetAnswersIntoAvailability() {
    when(fleetContextClient.findVehicleHealth(TENANT, VEHICLE))
        .thenReturn(Optional.of(new FleetVehicleHealth("DUE_SOON")));
    when(fleetContextClient.checkDriverEligibility(TENANT, DRIVER))
        .thenReturn(Optional.of(new FleetDriverEligibility(true)));

    Availability availability = service().check(TENANT, VEHICLE, DRIVER);

    assertThat(availability).isEqualTo(new Availability(MaintenanceState.DUE_SOON, true));
  }

  @Test
  void aVehicleUnknownToFleetIsNotFound() {
    when(fleetContextClient.findVehicleHealth(TENANT, VEHICLE)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().check(TENANT, VEHICLE, DRIVER))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("El vehículo no existe.");
  }

  @Test
  void aDriverUnknownToFleetIsNotFound() {
    when(fleetContextClient.findVehicleHealth(TENANT, VEHICLE))
        .thenReturn(Optional.of(new FleetVehicleHealth("UP_TO_DATE")));
    when(fleetContextClient.checkDriverEligibility(TENANT, DRIVER)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().check(TENANT, VEHICLE, DRIVER))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("El conductor no existe.");
  }

  @Test
  void anUnavailableFleetIsPropagatedAsUnavailable() {
    when(fleetContextClient.findVehicleHealth(TENANT, VEHICLE))
        .thenThrow(new FleetUnavailableException());

    assertThatThrownBy(() -> service().check(TENANT, VEHICLE, DRIVER))
        .isInstanceOf(FleetUnavailableException.class);
  }

  @Test
  void aMaintenanceStateTripDoesNotKnowIsAContractFailure() {
    when(fleetContextClient.findVehicleHealth(TENANT, VEHICLE))
        .thenReturn(Optional.of(new FleetVehicleHealth("BROKEN")));
    when(fleetContextClient.checkDriverEligibility(TENANT, DRIVER))
        .thenReturn(Optional.of(new FleetDriverEligibility(true)));

    assertThatThrownBy(() -> service().check(TENANT, VEHICLE, DRIVER))
        .isInstanceOf(FleetUnavailableException.class);
  }

  @Test
  void findsTheDriverOfAUserThroughFleet() {
    when(fleetContextClient.findDriverIdByUserId(TENANT, USER)).thenReturn(Optional.of(DRIVER));

    assertThat(service().findDriverIdByUser(TENANT, USER)).contains(DRIVER);
  }

  private FleetAvailabilityAclService service() {
    return new FleetAvailabilityAclService(fleetContextClient);
  }
}
