package com.nexus.rinde.trip.infrastructure.persistence.flyway;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Migraciones del esquema trip, con historial separado por contexto. */
@Configuration
public class TripFlywayConfiguration {

  @Bean(initMethod = "migrate")
  public Flyway tripFlyway(DataSource dataSource) {
    return Flyway.configure()
        .dataSource(dataSource)
        .schemas("trip")
        .defaultSchema("trip")
        .createSchemas(true)
        .locations("classpath:db/migration/trip")
        .load();
  }

  /** Hibernate debe arrancar después de que Flyway cree las tablas. */
  @Bean
  public static EntityManagerFactoryDependsOnPostProcessor tripFlywayDependsOn() {
    return new EntityManagerFactoryDependsOnPostProcessor("tripFlyway");
  }
}
