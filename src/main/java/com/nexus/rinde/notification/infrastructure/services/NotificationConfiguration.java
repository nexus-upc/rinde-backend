package com.nexus.rinde.notification.infrastructure.services;

import com.nexus.rinde.notification.domain.model.valueobjects.RetryPolicy;
import com.nexus.rinde.notification.domain.services.PushAdapter;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra el adaptador pendiente mientras no se configure un proveedor push real. */
@Configuration
public class NotificationConfiguration {

  @Bean
  @ConditionalOnMissingBean(PushAdapter.class)
  public PushAdapter unavailablePushAdapter() {
    return new UnavailablePushAdapter();
  }

  /** Calendario de reintentos leído de rinde.notification.retry.delays-minutes. */
  @Bean
  public RetryPolicy retryPolicy(NotificationRetryProperties properties) {
    return new RetryPolicy(properties.delaysMinutes().stream().map(Duration::ofMinutes).toList());
  }
}
