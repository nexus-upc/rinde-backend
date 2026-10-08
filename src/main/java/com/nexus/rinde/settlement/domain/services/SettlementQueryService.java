package com.nexus.rinde.settlement.domain.services;

import com.nexus.rinde.settlement.domain.model.aggregates.Settlement;
import com.nexus.rinde.settlement.domain.model.queries.GetSettlementByIdQuery;
import com.nexus.rinde.settlement.domain.model.queries.GetSettlementByTripIdQuery;

/** Puerto de entrada para las consultas del contexto Settlement. */
public interface SettlementQueryService {

  Settlement handle(GetSettlementByIdQuery query);

  Settlement handle(GetSettlementByTripIdQuery query);
}
