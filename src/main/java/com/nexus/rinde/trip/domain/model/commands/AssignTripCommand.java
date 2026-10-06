package com.nexus.rinde.trip.domain.model.commands;

import java.util.UUID;

/** Datos para asignar vehículo y conductor a un viaje. */
public record AssignTripCommand(
    UUID tenantId,
    UUID tripId,
    UUID vehicleId,
    UUID driverId,
    UUID assignedBy,
    boolean confirmOverdueMaintenance) {}
