package com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.fleet.domain.model.aggregates.Vehicle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio JPA para el agregado Vehicle. */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

  Optional<Vehicle> findByIdAndTenantId(UUID id, UUID tenantId);

  List<Vehicle> findByTenantId(UUID tenantId);

  long countByTenantId(UUID tenantId);

  boolean existsByTenantIdAndPlateNumber(UUID tenantId, String plateNumber);

  Optional<Vehicle> findByTenantIdAndPlateNumber(UUID tenantId, String plateNumber);
}
