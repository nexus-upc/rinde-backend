package com.nexus.rinde.fleet.infrastructure.persistence.flyway;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Migraciones del esquema fleet, con historial separado por contexto. */
@Configuration
public class FleetFlywayConfiguration {

  @Bean(initMethod = "migrate")
  public Flyway fleetFlyway(DataSource dataSource) {
    return Flyway.configure()
        .dataSource(dataSource)
        .schemas("fleet")
        .defaultSchema("fleet")
        .createSchemas(true)
        .locations("classpath:db/migration/fleet")
        .load();
  }

  /** Hibernate debe arrancar después de que Flyway cree las tablas. */
  @Bean
  public static EntityManagerFactoryDependsOnPostProcessor fleetFlywayDependsOn() {
    return new EntityManagerFactoryDependsOnPostProcessor("fleetFlyway");
  }
}
