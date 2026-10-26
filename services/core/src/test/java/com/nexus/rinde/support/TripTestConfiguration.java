package com.nexus.rinde.support;

import com.nexus.rinde.iam.interfaces.acl.IamContextFacade;
import com.nexus.rinde.trip.application.internal.outboundservices.acl.FleetAvailabilityAclService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Registra el doble de Fleet como dependencia principal durante las pruebas de aceptación. */
@TestConfiguration
public class TripTestConfiguration {

  @Bean
  @Primary
  public TestFleetAvailabilityService testFleetAvailabilityService(
      IamContextFacade iamContextFacade, FleetAvailabilityAclService realAdapter) {
    return new TestFleetAvailabilityService(iamContextFacade, realAdapter);
  }
}
