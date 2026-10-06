package com.nexus.rinde.acceptance;

import com.nexus.rinde.support.AbstractIntegrationTest;
import com.nexus.rinde.support.TripTestConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Levanta el contexto de Spring para Cucumber. Extiende la base de integración para compartir la
 * misma configuración (y el mismo contexto en caché) y apuntar a la base rinde_test.
 */
@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@Import(TripTestConfiguration.class)
public class CucumberSpringConfiguration extends AbstractIntegrationTest {}
