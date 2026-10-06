package com.nexus.rinde.support;

import com.nexus.rinde.iam.interfaces.acl.IamContextFacade;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Registra el doble de Fleet como dependencia principal durante las pruebas de aceptación. */
@TestConfiguration
public class TripTestConfiguration {

  @Bean
  @Primary
  public TestFleetAvailabilityService testFleetAvailabilityService(
      IamContextFacade iamContextFacade) {
    return new TestFleetAvailabilityService(iamContextFacade);
  }
}
