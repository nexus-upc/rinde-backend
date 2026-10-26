package com.nexus.rinde.trip.interfaces.rest.transform;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import com.nexus.rinde.trip.domain.model.valueobjects.TripStatus;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Convierte el filtro CSV de estados y rechaza valores que no forman parte del ciclo del viaje. */
public final class TripStatusFilterFromParameterAssembler {

  private TripStatusFilterFromParameterAssembler() {}

  public static Set<TripStatus> toStatuses(String parameter) {
    if (parameter == null || parameter.isBlank()) {
      return Set.of();
    }
    try {
      return Arrays.stream(parameter.split(",", -1))
          .map(String::trim)
          .map(TripStatus::valueOf)
          .collect(Collectors.toUnmodifiableSet());
    } catch (IllegalArgumentException ex) {
      throw new BusinessRuleException("El parámetro status contiene un estado de viaje inválido.");
    }
  }
}
