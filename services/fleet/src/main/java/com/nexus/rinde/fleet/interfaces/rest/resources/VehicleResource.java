package com.nexus.rinde.fleet.interfaces.rest.resources;

import com.nexus.rinde.fleet.domain.model.valueobjects.VehicleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Recurso de salida con los datos de un vehículo. */
public record VehicleResource(
    UUID id,
    String plateNumber,
    String brand,
    String model,
    Integer modelYear,
    BigDecimal payloadCapacityKg,
    VehicleStatus status,
    Instant createdAt) {}
