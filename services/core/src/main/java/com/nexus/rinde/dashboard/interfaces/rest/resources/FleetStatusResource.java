package com.nexus.rinde.dashboard.interfaces.rest.resources;

import java.util.List;

/** Estado consolidado de la flota de la empresa (US37). */
public record FleetStatusResource(
    int totalVehicles,
    int availableVehicles,
    int inTripVehicles,
    int inMaintenanceVehicles,
    int totalDrivers,
    int enabledDrivers,
    int disabledDrivers,
    List<VehicleSummary> vehicles,
    List<DriverSummary> drivers) {

  public record VehicleSummary(
      String id,
      String plateNumber,
      String brand,
      String model,
      String status) {}

  public record DriverSummary(
      String id,
      String fullName,
      String licenseNumber,
      String licenseCategory,
      String status) {}
}
