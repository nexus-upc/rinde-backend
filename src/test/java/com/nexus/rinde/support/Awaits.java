package com.nexus.rinde.support;

import java.time.Duration;
import java.util.function.BooleanSupplier;

/** Espera una condición que llega por mensajería, con un tiempo máximo. */
public final class Awaits {

  private Awaits() {}

  public static void until(BooleanSupplier condition, Duration timeout) {
    long deadline = System.nanoTime() + timeout.toNanos();
    while (!condition.getAsBoolean()) {
      if (System.nanoTime() > deadline) {
        throw new AssertionError("La condición no se cumplió en " + timeout.toSeconds() + " s.");
      }
      try {
        Thread.sleep(50);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new AssertionError("La espera fue interrumpida.", ex);
      }
    }
  }
}
