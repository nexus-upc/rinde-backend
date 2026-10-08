package com.nexus.rinde.settlement.interfaces.acl;

import com.nexus.rinde.settlement.domain.model.aggregates.Settlement;
import java.util.Optional;
import java.util.UUID;

/** Fachada pública del Bounded Context Settlement para otros contextos. */
public interface SettlementContextFacade {

  Optional<Settlement> findByTripIdAndTenantId(UUID tripId, UUID tenantId);
}
