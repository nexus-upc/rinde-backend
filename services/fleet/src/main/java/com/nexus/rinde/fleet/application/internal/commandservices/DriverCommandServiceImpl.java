package com.nexus.rinde.fleet.application.internal.commandservices;

import com.nexus.rinde.fleet.domain.model.aggregates.Driver;
import com.nexus.rinde.fleet.domain.model.commands.RegisterDriverCommand;
import com.nexus.rinde.fleet.domain.services.DriverCommandService;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.DriverRepository;
import com.nexus.rinde.shared.domain.exceptions.ConflictException;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación del servicio de aplicación de comandos para Driver. */
@Service
@Transactional
public class DriverCommandServiceImpl implements DriverCommandService {

  private final DriverRepository driverRepository;

  public DriverCommandServiceImpl(DriverRepository driverRepository) {
    this.driverRepository = driverRepository;
  }

  @Override
  public Driver handle(RegisterDriverCommand command) {
    if (driverRepository.existsByTenantIdAndDocumentNumber(
        command.tenantId(), command.documentNumber().trim())) {
      throw new ConflictException("El documento ya se encuentra registrado en la empresa.");
    }

    if (driverRepository.existsByTenantIdAndLicenseNumber(
        command.tenantId(), command.licenseNumber().trim().toUpperCase())) {
      throw new ConflictException("La licencia ya se encuentra registrada en la empresa.");
    }

    Driver driver =
        new Driver(
            command.tenantId(),
            command.userId(),
            command.fullName(),
            command.documentType(),
            command.documentNumber(),
            command.licenseNumber(),
            command.licenseCategory(),
            command.licenseExpirationDate(),
            LocalDate.now());

    return driverRepository.save(driver);
  }
}
