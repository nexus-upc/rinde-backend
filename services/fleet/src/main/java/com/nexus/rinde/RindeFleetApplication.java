package com.nexus.rinde;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada del servicio de Fleet & Maintenance. El paquete raíz com.nexus.rinde carga los
 * beans de rinde-shared-kernel. La autenticación es solo por JWT.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class RindeFleetApplication {

  public static void main(String[] args) {
    SpringApplication.run(RindeFleetApplication.class, args);
  }
}
