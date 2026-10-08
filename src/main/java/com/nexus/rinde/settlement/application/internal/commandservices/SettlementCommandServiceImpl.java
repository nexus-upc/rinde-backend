package com.nexus.rinde.settlement.application.internal.commandservices;

import com.nexus.rinde.settlement.domain.model.aggregates.Settlement;
import com.nexus.rinde.settlement.domain.model.commands.RegisterAdvanceCommand;
import com.nexus.rinde.settlement.domain.services.SettlementCommandService;
import com.nexus.rinde.settlement.infrastructure.persistence.jpa.repositories.SettlementRepository;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Coordina los comandos de liquidación con su persistencia. */
@Service
public class SettlementCommandServiceImpl implements SettlementCommandService {

  private final SettlementRepository settlementRepository;
  private final Clock clock;

  public SettlementCommandServiceImpl(SettlementRepository settlementRepository, Clock clock) {
    this.settlementRepository = settlementRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Settlement handle(RegisterAdvanceCommand command) {
    Settlement settlement =
        settlementRepository
            .findByTripIdAndTenantId(command.tripId(), command.tenantId())
            .orElseThrow(() -> new ResourceNotFoundException("No existe liquidación para este viaje."));

    settlement.registerAdvance(command.amount(), command.currency(), clock.instant());
    return settlementRepository.saveAndFlush(settlement);
  }
}
