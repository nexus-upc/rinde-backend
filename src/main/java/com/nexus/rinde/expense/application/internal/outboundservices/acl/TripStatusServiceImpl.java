package com.nexus.rinde.expense.application.internal.outboundservices.acl;

import com.nexus.rinde.expense.domain.services.TripStatusService;
import com.nexus.rinde.trip.interfaces.acl.TripContextFacade;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Adaptador ACL que consulta el estado del viaje a través de TripContextFacade. */
@Service
public class TripStatusServiceImpl implements TripStatusService {

  private final TripContextFacade tripContextFacade;

  public TripStatusServiceImpl(TripContextFacade tripContextFacade) {
    this.tripContextFacade = tripContextFacade;
  }

  @Override
  public Optional<String> findTripStatus(UUID tenantId, UUID tripId) {
    return tripContextFacade.findByIdAndTenantId(tripId, tenantId).map(summary -> summary.status());
  }
}
