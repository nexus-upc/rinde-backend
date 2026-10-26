package com.nexus.rinde.trip.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.trip.infrastructure.persistence.jpa.entities.ProcessedEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Registro de eventos procesados por Trip, identificados por su eventId. */
@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {}
