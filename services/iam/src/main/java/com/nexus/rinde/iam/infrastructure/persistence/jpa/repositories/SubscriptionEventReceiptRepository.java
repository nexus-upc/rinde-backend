package com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.iam.infrastructure.persistence.jpa.entities.SubscriptionEventReceipt;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Consulta duplicados y la última fecha recibida por empresa. */
public interface SubscriptionEventReceiptRepository extends JpaRepository<SubscriptionEventReceipt, UUID> {
  Optional<SubscriptionEventReceipt> findFirstByTenantIdOrderByOccurredAtDesc(UUID tenantId);
}
