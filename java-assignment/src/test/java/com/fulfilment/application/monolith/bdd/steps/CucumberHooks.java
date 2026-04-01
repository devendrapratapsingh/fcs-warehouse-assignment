package com.fulfilment.application.monolith.bdd.steps;

import io.cucumber.java.Before;
import io.restassured.RestAssured;

/**
 * Cucumber lifecycle hooks — run once before each scenario.
 *
 * Sets the RestAssured base URI/port to the fixed Quarkus test port (8081),
 * so all step definition HTTP calls reach the running Quarkus instance.
 */
public class CucumberHooks {

  /** Quarkus test port — must match quarkus.http.test-port in application.properties */
  private static final int QUARKUS_TEST_PORT = 8081;

  @Before
  public void configureRestAssured() {
    RestAssured.port = QUARKUS_TEST_PORT;
    RestAssured.baseURI = "http://localhost";
  }
}
