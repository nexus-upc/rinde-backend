package com.nexus.rinde;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada del monolito modular de RINDE: cada bounded context vive en su propio paquete.
 * La autenticación es solo por JWT, por eso se excluye el usuario en memoria de Spring Security.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class RindeApplication {

  public static void main(String[] args) {
    SpringApplication.run(RindeApplication.class, args);
  }
}
