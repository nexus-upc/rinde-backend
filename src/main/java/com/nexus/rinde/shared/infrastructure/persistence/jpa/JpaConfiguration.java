package com.nexus.rinde.shared.infrastructure.persistence.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Registra las entidades y repositorios de todos los bounded contexts, que comparten un solo
 * DataSource.
 */
@Configuration
@EntityScan("com.nexus.rinde")
@EnableJpaRepositories("com.nexus.rinde")
public class JpaConfiguration {}
