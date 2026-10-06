package com.nexus.rinde.shared.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.data.domain.Persistable;

/**
 * Clase base de las raíces de agregado: identificador UUID, fechas de auditoría y eventos de
 * dominio pendientes, que Spring Data publica al guardar el agregado.
 */
@MappedSuperclass
public abstract class AuditableAbstractAggregateRoot implements Persistable<UUID> {

  @Id private UUID id;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  /** El id se asigna al crear el objeto, por eso hay que indicar a Spring Data si es nuevo. */
  @Transient private boolean newEntity = true;

  @Transient private final List<Object> domainEvents = new ArrayList<>();

  protected AuditableAbstractAggregateRoot() {
    this.id = UUID.randomUUID();
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return newEntity;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  /** Registra un evento que se publicará cuando el agregado se guarde. */
  protected void registerEvent(Object event) {
    domainEvents.add(event);
  }

  @DomainEvents
  public List<Object> pendingDomainEvents() {
    return List.copyOf(domainEvents);
  }

  @AfterDomainEventPublication
  public void clearDomainEvents() {
    domainEvents.clear();
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  @PostLoad
  @PostPersist
  void markNotNew() {
    newEntity = false;
  }
}
