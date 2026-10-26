package com.nexus.rinde.fleet.application.internal.eventhandlers;

import com.nexus.rinde.fleet.domain.model.aggregates.Vehicle;
import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.nexus.rinde.trip.domain.model.events.TripFinished;
import com.nexus.rinde.trip.domain.model.events.TripStarted;
import java.util.UUID;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Refleja en Fleet el inicio y fin de cada viaje. Corre después del commit del viaje, en su propia
 * transacción, y ningún fallo aquí puede revertir la operación de Trip.
 */
@Component
public class TripEventHandler {

  private static final Logger log = LoggerFactory.getLogger(TripEventHandler.class);

  private final VehicleRepository vehicleRepository;
  private final TransactionTemplate transactionTemplate;

  public TripEventHandler(
      VehicleRepository vehicleRepository, PlatformTransactionManager transactionManager) {
    this.vehicleRepository = vehicleRepository;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(TripStarted event) {
    update(event.tenantId(), event.vehicleId(), "TripStarted", Vehicle::startTrip);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(TripFinished event) {
    update(event.tenantId(), event.vehicleId(), "TripFinished", Vehicle::finishTrip);
  }

  private void update(UUID tenantId, UUID vehicleId, String eventName, Consumer<Vehicle> change) {
    try {
      transactionTemplate.executeWithoutResult(
          status ->
              vehicleRepository
                  .findByIdAndTenantId(vehicleId, tenantId)
                  .ifPresentOrElse(
                      change,
                      () ->
                          log.debug(
                              "El evento {} referencia una unidad que no existe: {}.",
                              eventName,
                              vehicleId)));
    } catch (RuntimeException ex) {
      log.error("No se pudo actualizar la unidad del evento {}.", eventName, ex);
    }
  }
}
