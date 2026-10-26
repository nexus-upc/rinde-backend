package com.nexus.rinde.trip.infrastructure.configuration;

import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configura la publicación de eventos de Trip sin revertir comandos confirmados. */
@Configuration
public class TripEventConfiguration {

  @Bean(name = "applicationEventMulticaster")
  public static SimpleApplicationEventMulticaster applicationEventMulticaster() {
    return new TripSafeApplicationEventMulticaster();
  }
}
