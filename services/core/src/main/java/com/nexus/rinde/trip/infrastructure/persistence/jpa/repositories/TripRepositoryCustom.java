package com.nexus.rinde.trip.infrastructure.persistence.jpa.repositories;

import java.util.UUID;

/** Operaciones de persistencia específicas de Trip Management. */
public interface TripRepositoryCustom {

  String nextCodeForTenant(UUID tenantId);
}
