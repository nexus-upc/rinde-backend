package com.nexus.rinde.notification.application.internal.commandservices;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Revisa los avisos vencidos a intervalos fijos; la lógica vive en NotificationRetryService. */
@Component
public class NotificationRetryJob {

  private final NotificationRetryService retryService;

  public NotificationRetryJob(NotificationRetryService retryService) {
    this.retryService = retryService;
  }

  @Scheduled(fixedDelayString = "${rinde.notification.retry.poll-delay-ms:30000}")
  public void runRetries() {
    retryService.retryDueNotices();
  }
}
