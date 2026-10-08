package com.nexus.rinde.settlement.application.internal.queryservices;

import com.nexus.rinde.settlement.domain.model.aggregates.Settlement;
import com.nexus.rinde.settlement.domain.model.queries.GetSettlementByIdQuery;
import com.nexus.rinde.settlement.domain.model.queries.GetSettlementByTripIdQuery;
import com.nexus.rinde.settlement.domain.services.SettlementQueryService;
import com.nexus.rinde.settlement.infrastructure.persistence.jpa.repositories.SettlementRepository;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Atiende consultas sobre las liquidaciones registradas. */
@Service
public class SettlementQueryServiceImpl implements SettlementQueryService {

  private final SettlementRepository settlementRepository;

  public SettlementQueryServiceImpl(SettlementRepository settlementRepository) {
    this.settlementRepository = settlementRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Settlement handle(GetSettlementByIdQuery query) {
    return settlementRepository
        .findByIdAndTenantId(query.settlementId(), query.tenantId())
        .orElseThrow(() -> new ResourceNotFoundException("La liquidación no existe."));
  }

  @Override
  @Transactional(readOnly = true)
  public Settlement handle(GetSettlementByTripIdQuery query) {
    return settlementRepository
        .findByTripIdAndTenantId(query.tripId(), query.tenantId())
        .orElseThrow(() -> new ResourceNotFoundException("No existe liquidación para este viaje."));
  }
}
