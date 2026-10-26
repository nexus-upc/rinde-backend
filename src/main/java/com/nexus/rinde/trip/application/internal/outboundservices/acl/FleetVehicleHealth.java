package com.nexus.rinde.trip.application.internal.outboundservices.acl;

/** Estado de mantenimiento de una unidad tal como lo informa Fleet. Trip no conoce su modelo. */
public record FleetVehicleHealth(String maintenanceState) {}
