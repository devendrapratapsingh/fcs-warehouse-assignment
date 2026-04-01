package com.fulfilment.application.monolith.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.core.cli.Main;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

/**
 * Cucumber BDD integration test runner for Quarkus.
 *
 * Strategy:
 *  - @QuarkusTest starts the full Quarkus application (Dev Services Postgres, all CDI beans).
 *  - @TestProfile(CucumberTestProfile) gives this runner its own isolated Dev Services DB so
 *    that BDD scenario mutations (creates/updates/deletes) do NOT pollute the shared DB used
 *    by ProductResourceTest, StoreResourceTest, WarehouseEndpointTest, etc.
 *  - The single @Test method invokes the Cucumber CLI programmatically in the SAME JVM.
 *  - RestAssured is pointed at the already-running server on port 8081 before Cucumber runs.
 *  - The Cucumber engine discovers all .feature files under src/test/resources/features/
 *    and step definitions in the bdd.steps package.
 *
 * Reports written to:
 *  - target/cucumber-reports/cucumber.html  (HTML)
 *  - target/cucumber-reports/cucumber.json  (JSON — for CI badge/reporting tools)
 *
 * Run BDD tests only:  ./mvnw test -Dtest=CucumberIT
 * Run all tests:        ./mvnw test
 */
@QuarkusTest
@TestProfile(CucumberTestProfile.class)
public class CucumberIT {

  @Test
  void runCucumberScenarios() {
    // Point RestAssured at the Quarkus test server started by @QuarkusTest above
    RestAssured.port = 8081;
    RestAssured.baseURI = "http://localhost";

    // Run all feature files; exit code 0 = all passed, non-zero = failures
    byte exitCode = Main.run(new String[]{
        "--glue", "com.fulfilment.application.monolith.bdd.steps",
        "--plugin", "pretty",
        "--plugin", "html:target/cucumber-reports/cucumber.html",
        "--plugin", "json:target/cucumber-reports/cucumber.json",
        "classpath:features"
    }, Thread.currentThread().getContextClassLoader());

    assertEquals(0, exitCode,
        "Cucumber scenarios failed — see target/cucumber-reports/cucumber.html for details.");
  }
}
