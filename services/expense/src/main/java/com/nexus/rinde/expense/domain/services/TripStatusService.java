package com.nexus.rinde.expense.domain.services;

import java.util.Optional;
import java.util.UUID;

/** Consulta el estado de un viaje ajeno al contexto, filtrado por empresa. */
public interface TripStatusService {

  /** Devuelve el nombre del estado del viaje, o vacío si no existe para la empresa. */
  Optional<String> findTripStatus(UUID tenantId, UUID tripId);
}
