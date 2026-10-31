package com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.fleet.domain.model.entities.Maintenance;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio JPA para la entidad Maintenance. */
@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID> {

  List<Maintenance> findByTenantIdAndVehicleId(UUID tenantId, UUID vehicleId);

  List<Maintenance> findByTenantId(UUID tenantId);
}
