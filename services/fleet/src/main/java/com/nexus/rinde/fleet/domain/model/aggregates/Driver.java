package com.nexus.rinde.fleet.domain.model.aggregates;

import com.nexus.rinde.fleet.domain.model.events.DriverRegistered;
import com.nexus.rinde.fleet.domain.model.valueobjects.DriverEligibility;
import com.nexus.rinde.fleet.domain.model.valueobjects.DriverStatus;
import com.nexus.rinde.fleet.domain.model.valueobjects.LicenseStatus;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import com.nexus.rinde.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.UUID;

/** Conductor registrado en la flota y raíz del agregado Driver. */
@Entity
@Table(
    schema = "fleet",
    name = "drivers",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_drivers_tenant_document",
          columnNames = {"tenant_id", "document_number"}),
      @UniqueConstraint(
          name = "uk_drivers_tenant_license",
          columnNames = {"tenant_id", "license_number"})
    },
    indexes = {
      @Index(name = "idx_drivers_tenant_status", columnList = "tenant_id, status"),
      @Index(name = "idx_drivers_tenant_user", columnList = "tenant_id, user_id")
    })
public class Driver extends AuditableAbstractAggregateRoot {

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private UUID tenantId;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "full_name", nullable = false, length = 120)
  private String fullName;

  @Column(name = "document_type", nullable = false, length = 10)
  private String documentType;

  @Column(name = "document_number", nullable = false, length = 20)
  private String documentNumber;

  @Column(name = "license_number", nullable = false, length = 20)
  private String licenseNumber;

  @Column(name = "license_category", nullable = false, length = 10)
  private String licenseCategory;

  @Column(name = "license_expiration_date", nullable = false)
  private LocalDate licenseExpirationDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private DriverStatus status;

  protected Driver() {}

  public Driver(
      UUID tenantId,
      UUID userId,
      String fullName,
      String documentType,
      String documentNumber,
      String licenseNumber,
      String licenseCategory,
      LocalDate licenseExpirationDate,
      LocalDate today) {
    if (tenantId == null) {
      throw new BusinessRuleException("El identificador de empresa es obligatorio.");
    }
    if (fullName == null || fullName.trim().length() < 3) {
      throw new BusinessRuleException("El nombre del conductor no es válido.");
    }
    if (documentType == null || documentType.isBlank()) {
      throw new BusinessRuleException("El tipo de documento es obligatorio.");
    }
    if (documentNumber == null || documentNumber.isBlank()) {
      throw new BusinessRuleException("El número de documento es obligatorio.");
    }
    if (licenseNumber == null || licenseNumber.isBlank()) {
      throw new BusinessRuleException("El número de licencia es obligatorio.");
    }
    if (licenseCategory == null || licenseCategory.isBlank()) {
      throw new BusinessRuleException("La categoría de la licencia es obligatoria.");
    }
    if (licenseExpirationDate == null) {
      throw new BusinessRuleException("La fecha de vencimiento de la licencia es obligatoria.");
    }

    this.tenantId = tenantId;
    this.userId = userId;
    this.fullName = fullName.trim();
    this.documentType = documentType.trim().toUpperCase();
    this.documentNumber = documentNumber.trim();
    this.licenseNumber = licenseNumber.trim().toUpperCase();
    this.licenseCategory = licenseCategory.trim().toUpperCase();
    this.licenseExpirationDate = licenseExpirationDate;

    // US13: si la fecha de vencimiento es anterior a la fecha actual, se registra pero no queda habilitado
    if (today != null && licenseExpirationDate.isBefore(today)) {
      this.status = DriverStatus.DISABLED;
    } else {
      this.status = DriverStatus.ENABLED;
    }

    registerEvent(new DriverRegistered(this.getId(), this.tenantId, this.licenseNumber));
  }

  public DriverEligibility checkEligibility(LocalDate today) {
    boolean expired = today != null && this.licenseExpirationDate.isBefore(today);
    LicenseStatus licStatus = expired ? LicenseStatus.EXPIRED : LicenseStatus.VALID;

    if (expired) {
      return new DriverEligibility(
          this.getId(),
          false,
          this.status,
          licStatus,
          "El conductor no está habilitado para ser asignado.");
    }
    if (this.status != DriverStatus.ENABLED) {
      return new DriverEligibility(
          this.getId(),
          false,
          this.status,
          licStatus,
          "El conductor no está habilitado para ser asignado.");
    }
    return new DriverEligibility(this.getId(), true, this.status, licStatus, "Conductor habilitado.");
  }

  public void enable() {
    this.status = DriverStatus.ENABLED;
  }

  public void disable() {
    this.status = DriverStatus.DISABLED;
  }

  public void updateUserId(UUID userId) {
    this.userId = userId;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getFullName() {
    return fullName;
  }

  public String getDocumentType() {
    return documentType;
  }

  public String getDocumentNumber() {
    return documentNumber;
  }

  public String getLicenseNumber() {
    return licenseNumber;
  }

  public String getLicenseCategory() {
    return licenseCategory;
  }

  public LocalDate getLicenseExpirationDate() {
    return licenseExpirationDate;
  }

  public DriverStatus getStatus() {
    return status;
  }
}
