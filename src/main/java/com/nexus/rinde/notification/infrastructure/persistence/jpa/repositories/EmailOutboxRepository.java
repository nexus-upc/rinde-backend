package com.nexus.rinde.notification.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.notification.infrastructure.persistence.jpa.entities.EmailOutboxEntry;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Almacena de forma idempotente comprobantes y recordatorios de correo. */
@Repository
public interface EmailOutboxRepository extends JpaRepository<EmailOutboxEntry, UUID> {}
