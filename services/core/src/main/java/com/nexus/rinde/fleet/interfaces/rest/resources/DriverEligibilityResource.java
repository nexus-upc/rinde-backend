package com.nexus.rinde.fleet.interfaces.rest.resources;

import com.nexus.rinde.fleet.domain.model.valueobjects.DriverStatus;
import com.nexus.rinde.fleet.domain.model.valueobjects.LicenseStatus;
import java.util.UUID;

/** Recurso de salida para la consulta de elegibilidad del conductor (US13, US15). */
public record DriverEligibilityResource(
    UUID driverId,
    boolean eligible,
    DriverStatus driverStatus,
    LicenseStatus licenseStatus,
    String reason) {}
