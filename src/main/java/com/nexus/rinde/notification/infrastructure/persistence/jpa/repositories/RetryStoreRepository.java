package com.nexus.rinde.notification.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.notification.infrastructure.persistence.jpa.entities.RetryStoreEntry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Persistencia operativa para idempotencia y avisos push pendientes de reintento. */
@Repository
public interface RetryStoreRepository extends JpaRepository<RetryStoreEntry, UUID> {

  List<RetryStoreEntry> findAllByDeliveryStatusAndNextAttemptAtLessThanEqual(
      String deliveryStatus, Instant nextAttemptAt);
}
