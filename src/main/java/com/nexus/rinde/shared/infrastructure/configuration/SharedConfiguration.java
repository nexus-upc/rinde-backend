package com.nexus.rinde.shared.infrastructure.configuration;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans comunes a todos los contextos; el reloj se inyecta para poder fijar la hora en las pruebas.
 */
@Configuration
public class SharedConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
