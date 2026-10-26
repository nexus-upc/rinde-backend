package com.nexus.rinde.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Puerta de enlace de RINDE: recibe todo el tráfico /api/v1 y lo reenvía a IAM o al monolito. */
@SpringBootApplication
public class RindeGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(RindeGatewayApplication.class, args);
  }
}
