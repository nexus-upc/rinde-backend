package com.nexus.rinde.fleet.application.internal.commandservices;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Revisa los mantenimientos cada día; la lógica vive en MaintenanceDueAnnouncementService. */
@Component
public class MaintenanceDueJob {

  private final MaintenanceDueAnnouncementService announcementService;

  public MaintenanceDueJob(MaintenanceDueAnnouncementService announcementService) {
    this.announcementService = announcementService;
  }

  @Scheduled(cron = "${rinde.fleet.maintenance-alerts.cron:0 0 6 * * *}")
  public void runDaily() {
    announcementService.announceDueMaintenances();
  }
}
