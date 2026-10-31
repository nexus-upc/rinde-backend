package com.nexus.rinde.expense.infrastructure.persistence.flyway;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Migraciones del esquema expense, con historial separado por contexto. */
@Configuration
public class ExpenseFlywayConfiguration {

  @Bean(initMethod = "migrate")
  public Flyway expenseFlyway(DataSource dataSource) {
    return Flyway.configure()
        .dataSource(dataSource)
        .schemas("expense")
        .defaultSchema("expense")
        .createSchemas(true)
        .locations("classpath:db/migration/expense")
        .load();
  }

  /** Hibernate debe arrancar después de que Flyway cree las tablas. */
  @Bean
  public static EntityManagerFactoryDependsOnPostProcessor expenseFlywayDependsOn() {
    return new EntityManagerFactoryDependsOnPostProcessor("expenseFlyway");
  }
}
