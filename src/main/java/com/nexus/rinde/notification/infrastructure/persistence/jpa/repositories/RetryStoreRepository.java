package com.nexus.rinde.notification.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.notification.infrastructure.persistence.jpa.entities.RetryStoreEntry;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Persistencia operativa para idempotencia y avisos push pendientes. */
@Repository
public interface RetryStoreRepository extends JpaRepository<RetryStoreEntry, UUID> {}
