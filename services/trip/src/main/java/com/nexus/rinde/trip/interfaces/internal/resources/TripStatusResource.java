package com.nexus.rinde.trip.interfaces.internal.resources;

import java.util.UUID;

/** Estado actual de un viaje, consultado por Expense para bloquear gastos fuera de ruta. */
public record TripStatusResource(UUID tripId, String status) {}
