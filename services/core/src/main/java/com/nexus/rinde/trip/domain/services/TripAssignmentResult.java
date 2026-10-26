package com.nexus.rinde.trip.domain.services;

import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.domain.model.valueobjects.MaintenanceState;

/** Resultado de asignar: incluye la alerta de mantenimiento que debe ver el responsable. */
public record TripAssignmentResult(Trip trip, MaintenanceState maintenanceAlert) {}
