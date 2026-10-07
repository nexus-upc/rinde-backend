package com.nexus.rinde.fleet.interfaces.rest.transform;

import com.nexus.rinde.fleet.domain.model.aggregates.Driver;
import com.nexus.rinde.fleet.domain.model.valueobjects.DriverEligibility;
import com.nexus.rinde.fleet.interfaces.rest.resources.DriverEligibilityResource;
import com.nexus.rinde.fleet.interfaces.rest.resources.DriverResource;

/** Transformadores de objetos de dominio Driver a recursos REST. */
public final class DriverResourceAssembler {

  private DriverResourceAssembler() {}

  public static DriverResource toResource(Driver entity) {
    return new DriverResource(
        entity.getId(),
        entity.getUserId(),
        entity.getFullName(),
        entity.getDocumentType(),
        entity.getDocumentNumber(),
        entity.getLicenseNumber(),
        entity.getLicenseCategory(),
        entity.getLicenseExpirationDate(),
        entity.getStatus(),
        entity.getCreatedAt());
  }

  public static DriverEligibilityResource toEligibilityResource(DriverEligibility eligibility) {
    return new DriverEligibilityResource(
        eligibility.driverId(),
        eligibility.eligible(),
        eligibility.driverStatus(),
        eligibility.licenseStatus(),
        eligibility.reason());
  }
}
