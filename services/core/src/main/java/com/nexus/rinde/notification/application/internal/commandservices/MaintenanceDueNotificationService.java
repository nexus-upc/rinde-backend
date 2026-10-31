package com.nexus.rinde.notification.application.internal.commandservices;

import com.nexus.rinde.fleet.interfaces.acl.MaintenanceDue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Convierte un mantenimiento próximo o vencido en un aviso para el administrador de la empresa. */
@Service
public class MaintenanceDueNotificationService {

  private static final String OVERDUE = "OVERDUE";

  private final AdministratorEmailOutbox emailOutbox;

  public MaintenanceDueNotificationService(AdministratorEmailOutbox emailOutbox) {
    this.emailOutbox = emailOutbox;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handle(MaintenanceDue event) {
    boolean overdue = OVERDUE.equals(event.maintenanceState());
    String state = overdue ? "vencido" : "próximo";
    String subject = "Mantenimiento " + state + " de la unidad " + event.plateNumber();
    String body =
        "La unidad con placa "
            + event.plateNumber()
            + " tiene un mantenimiento "
            + state
            + ". Fecha límite: "
            + event.nextMaintenanceDate()
            + ". Programa la revisión desde el módulo de flota de RINDE.";
    emailOutbox.queueOnce(event.eventId(), event.tenantId(), subject, body);
  }
}
