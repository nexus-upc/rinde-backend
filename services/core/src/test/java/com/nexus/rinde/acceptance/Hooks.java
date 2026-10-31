package com.nexus.rinde.acceptance;

import com.nexus.rinde.support.CapturedEvents;
import com.nexus.rinde.support.TestDatabase;
import io.cucumber.java.Before;

/** Deja la base limpia antes de cada escenario para poder repetir las pruebas sin datos previos. */
public class Hooks {

  private final TestDatabase testDatabase;
  private final CapturedEvents capturedEvents;

  public Hooks(TestDatabase testDatabase, CapturedEvents capturedEvents) {
    this.testDatabase = testDatabase;
    this.capturedEvents = capturedEvents;
  }

  @Before
  public void cleanData() {
    testDatabase.clean();
    capturedEvents.clear();
  }
}
