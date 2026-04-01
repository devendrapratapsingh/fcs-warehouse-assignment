package com.fulfilment.application.monolith.bdd;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * JUnit Platform Suite that boots Cucumber over the Quarkus test server.
 *
 * How it works:
 *  - @Suite makes JUnit 5 treat this class as a test suite aggregator.
 *  - @IncludeEngines("cucumber") delegates execution to the Cucumber JUnit Platform engine.
 *  - @SelectClasspathResource points at the features directory on the test classpath.
 *  - The step definitions under the `bdd.steps` package are auto-discovered via
 *    the cucumber.glue property.
 *  - Quarkus Dev Services (Postgres) is already running because the other @QuarkusTest
 *    classes in this Maven module start it; the suite reuses the same JVM process.
 *
 * Run with:  ./mvnw test -Dtest=CucumberSuiteRunner
 * Or simply: ./mvnw test   (Surefire picks it up automatically)
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = "com.fulfilment.application.monolith.bdd.steps")
@ConfigurationParameter(
    key = Constants.PLUGIN_PROPERTY_NAME,
    value = "pretty, html:target/cucumber-reports/cucumber.html, json:target/cucumber-reports/cucumber.json")
@ConfigurationParameter(
    key = Constants.FILTER_TAGS_PROPERTY_NAME,
    value = "not @Ignored")
public class CucumberSuiteRunner {
  // No body needed — JUnit Platform Suite drives execution.
}
