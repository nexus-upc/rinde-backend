package com.nexus.rinde.acceptance;

import com.nexus.rinde.support.CapturedEvents;
import com.nexus.rinde.support.FleetStub;
import com.nexus.rinde.support.TestDatabase;
import io.cucumber.java.Before;

/** Deja la base y el doble de Fleet limpios antes de cada escenario. */
public class Hooks {

  private final TestDatabase testDatabase;
  private final CapturedEvents capturedEvents;
  private final FleetStub fleetStub;

  public Hooks(TestDatabase testDatabase, CapturedEvents capturedEvents, FleetStub fleetStub) {
    this.testDatabase = testDatabase;
    this.capturedEvents = capturedEvents;
    this.fleetStub = fleetStub;
  }

  @Before
  public void cleanData() {
    testDatabase.clean();
    capturedEvents.clear();
    fleetStub.reset();
  }
}
