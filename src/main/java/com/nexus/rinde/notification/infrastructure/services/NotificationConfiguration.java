package com.nexus.rinde.notification.infrastructure.services;

import com.nexus.rinde.notification.domain.services.PushAdapter;
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
}
