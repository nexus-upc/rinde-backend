package com.nexus.rinde.trip.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.shared.domain.exceptions.ConflictException;
import jakarta.persistence.EntityManager;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Genera códigos correlativos por empresa con un bloqueo transaccional de PostgreSQL. */
@Repository
public class TripRepositoryImpl implements TripRepositoryCustom {

  private final EntityManager entityManager;

  public TripRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public String nextCodeForTenant(UUID tenantId) {
    entityManager
        .createNativeQuery(
            "select pg_advisory_xact_lock(hashtextextended(cast(?1 as text), 0))")
        .setParameter(1, tenantId.toString())
        .getSingleResult();
    String lastCode =
        (String)
            entityManager
                .createNativeQuery(
                    "select max(code) from trip.trips where tenant_id = ?1")
                .setParameter(1, tenantId)
                .getSingleResult();
    int nextNumber = lastCode == null ? 1 : Integer.parseInt(lastCode.substring(4)) + 1;
    if (nextNumber > 999_999) {
      // TODO: Confirmar con negocio el comportamiento al superar TRP-999999.
      throw new ConflictException("La empresa alcanzó el máximo de códigos de viaje disponibles.");
    }
    return String.format(Locale.ROOT, "TRP-%06d", nextNumber);
  }
}
