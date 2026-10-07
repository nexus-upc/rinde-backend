package com.nexus.rinde.fleet.domain.model.commands;

import java.math.BigDecimal;
import java.util.UUID;

/** Comando para registrar un vehículo en la flota. */
public record RegisterVehicleCommand(
    UUID tenantId,
    String plateNumber,
    String brand,
    String model,
    Integer modelYear,
    BigDecimal payloadCapacityKg) {}
