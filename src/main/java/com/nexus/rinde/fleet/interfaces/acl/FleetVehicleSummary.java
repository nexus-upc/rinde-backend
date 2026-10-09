package com.nexus.rinde.fleet.interfaces.acl;

import java.util.UUID;

/** Vista de una unidad para otros contextos. El estado viaja como nombre del enum. */
public record FleetVehicleSummary(
    UUID id, String plateNumber, String brand, String model, String status) {}
