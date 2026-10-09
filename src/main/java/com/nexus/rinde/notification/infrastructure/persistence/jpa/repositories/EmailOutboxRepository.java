package com.nexus.rinde.notification.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.notification.infrastructure.persistence.jpa.entities.EmailOutboxEntry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Almacena de forma idempotente comprobantes y recordatorios de correo, con sus reintentos. */
@Repository
public interface EmailOutboxRepository extends JpaRepository<EmailOutboxEntry, UUID> {

  List<EmailOutboxEntry> findAllByDeliveryStatusAndNextAttemptAtLessThanEqual(
      String deliveryStatus, Instant nextAttemptAt);
}
