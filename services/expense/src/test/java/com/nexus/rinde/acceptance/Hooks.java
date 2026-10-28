package com.nexus.rinde.acceptance;

import com.nexus.rinde.support.CapturedEvents;
import com.nexus.rinde.support.IamStub;
import com.nexus.rinde.support.TestDatabase;
import com.nexus.rinde.support.TripStub;
import io.cucumber.java.Before;

/** Deja la base limpia antes de cada escenario para poder repetir las pruebas sin datos previos. */
public class Hooks {

  private final TestDatabase testDatabase;
  private final CapturedEvents capturedEvents;
  private final IamStub iamStub;
  private final TripStub tripStub;

  public Hooks(
      TestDatabase testDatabase,
      CapturedEvents capturedEvents,
      IamStub iamStub,
      TripStub tripStub) {
    this.testDatabase = testDatabase;
    this.capturedEvents = capturedEvents;
    this.iamStub = iamStub;
    this.tripStub = tripStub;
  }

  @Before
  public void cleanData() {
    testDatabase.clean();
    capturedEvents.clear();
    iamStub.reset();
    tripStub.reset();
  }
}
