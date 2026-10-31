package com.nexus.rinde.notification.infrastructure.persistence.flyway;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Migraciones del registro operativo de notificaciones. */
@Configuration
public class NotificationFlywayConfiguration {

  @Bean(initMethod = "migrate")
  public Flyway notificationFlyway(DataSource dataSource) {
    return Flyway.configure()
        .dataSource(dataSource)
        .schemas("notification")
        .defaultSchema("notification")
        .createSchemas(true)
        .locations("classpath:db/migration/notification")
        .load();
  }

  /** Hibernate debe iniciar después de que Flyway cree las tablas. */
  @Bean
  public static EntityManagerFactoryDependsOnPostProcessor notificationFlywayDependsOn() {
    return new EntityManagerFactoryDependsOnPostProcessor("notificationFlyway");
  }
}
