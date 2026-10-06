package com.nexus.rinde.trip.application.internal.outboundservices.acl;

import com.nexus.rinde.iam.interfaces.acl.IamContextFacade;
import com.nexus.rinde.trip.domain.model.valueobjects.Availability;
import com.nexus.rinde.trip.domain.model.valueobjects.MaintenanceState;
import com.nexus.rinde.trip.domain.services.FleetAvailabilityService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Respuesta provisional de Fleet mientras su fachada no esté implementada. */
@Service
public class ProvisionalFleetAvailabilityService implements FleetAvailabilityService {

  private final IamContextFacade iamContextFacade;

  // TODO: Reemplazar esta implementación provisional por la fachada de Fleet & Maintenance.
  public ProvisionalFleetAvailabilityService(IamContextFacade iamContextFacade) {
    this.iamContextFacade = iamContextFacade;
  }

  @Override
  public Availability check(UUID tenantId, UUID vehicleId, UUID driverId) {
    return new Availability(MaintenanceState.UP_TO_DATE, true);
  }

  @Override
  public Optional<UUID> findDriverIdByUser(UUID tenantId, UUID userId) {
    return iamContextFacade.isActiveUserWithRole(tenantId, userId, "DRIVER")
        ? Optional.of(userId)
        : Optional.empty();
  }
}
