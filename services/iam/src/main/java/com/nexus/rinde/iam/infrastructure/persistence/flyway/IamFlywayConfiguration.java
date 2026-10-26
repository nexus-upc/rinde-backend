package com.nexus.rinde.iam.infrastructure.persistence.flyway;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Migraciones del esquema iam. Cada contexto declara su propio Flyway (esquema, carpeta e historial
 * propios) para poder extraerlo después a su propio servicio.
 */
@Configuration
public class IamFlywayConfiguration {

  @Bean(initMethod = "migrate")
  public Flyway iamFlyway(DataSource dataSource) {
    return Flyway.configure()
        .dataSource(dataSource)
        .schemas("iam")
        .defaultSchema("iam")
        .createSchemas(true)
        .locations("classpath:db/migration/iam")
        .load();
  }

  /** Hibernate debe arrancar después de que Flyway cree las tablas. */
  @Bean
  public static EntityManagerFactoryDependsOnPostProcessor iamFlywayDependsOn() {
    return new EntityManagerFactoryDependsOnPostProcessor("iamFlyway");
  }
}
