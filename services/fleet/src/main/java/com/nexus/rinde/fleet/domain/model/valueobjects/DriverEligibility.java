package com.nexus.rinde.fleet.domain.model.valueobjects;

import java.util.UUID;

/** Estado de elegibilidad de un conductor para ser asignado a un viaje. */
public record DriverEligibility(
    UUID driverId,
    boolean eligible,
    DriverStatus driverStatus,
    LicenseStatus licenseStatus,
    String reason) {}
