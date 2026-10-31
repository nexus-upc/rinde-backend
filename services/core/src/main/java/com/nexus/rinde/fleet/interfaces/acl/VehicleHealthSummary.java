package com.nexus.rinde.fleet.interfaces.acl;

import java.util.UUID;

/** Resumen de salud de una unidad expuesto a otros contextos. */
public record VehicleHealthSummary(UUID vehicleId, String maintenanceState, boolean availableForTrip) {}
