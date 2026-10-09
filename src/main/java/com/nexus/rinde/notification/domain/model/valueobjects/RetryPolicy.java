package com.nexus.rinde.notification.domain.model.valueobjects;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/** Calendario de reintentos: la espera de cada reintento sale de la lista según los reintentos ya realizados. */
public record RetryPolicy(List<Duration> delays) {

  public RetryPolicy {
    delays = List.copyOf(delays);
    if (delays.stream().anyMatch(delay -> delay.isNegative() || delay.isZero())) {
      throw new IllegalArgumentException("Las esperas de reintento deben ser mayores que cero.");
    }
  }

  /** Espera antes del siguiente reintento, o vacío si ya no corresponde reintentar. */
  public Optional<Duration> waitBeforeNextRetry(int retriesMade) {
    if (retriesMade < 0) {
      throw new IllegalArgumentException("El número de reintentos no puede ser negativo.");
    }
    return retriesMade < delays.size() ? Optional.of(delays.get(retriesMade)) : Optional.empty();
  }
}
