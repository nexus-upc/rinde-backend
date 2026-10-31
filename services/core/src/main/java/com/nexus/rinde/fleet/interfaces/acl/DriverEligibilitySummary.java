package com.nexus.rinde.fleet.interfaces.acl;

import java.util.UUID;

/** Resumen de elegibilidad de un conductor expuesto a otros contextos. */
public record DriverEligibilitySummary(UUID driverId, boolean eligible, String reason) {}
