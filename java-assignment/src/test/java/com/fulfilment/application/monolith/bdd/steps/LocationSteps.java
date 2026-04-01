package com.fulfilment.application.monolith.bdd.steps;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.http.ContentType;
import java.util.UUID;

/**
 * Step definitions for location resolution scenarios.
 *
 * LocationGateway is tested indirectly via the warehouse creation endpoint —
 * CreateWarehouseUseCase calls LocationGateway.resolveByIdentifier internally,
 * so the HTTP response code reflects gateway behaviour.
 *
 * A UUID-based probe code avoids collision across scenarios that share the same DB.
 */
public class LocationSteps {

  private final ScenarioContext ctx;
  private String resolvedIdentifier;
  private String pendingUnknownIdentifier;

  public LocationSteps(ScenarioContext ctx) {
    this.ctx = ctx;
  }

  @Given("I resolve location {string}")
  public void resolveLocation(String identifier) {
    // Unique 8-char suffix so parallel/sequential scenarios never collide
    String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    String probeCode = "PRB-" + suffix;

    String body = "{"
        + "\"businessUnitCode\":\"" + probeCode + "\","
        + "\"location\":\"" + identifier + "\","
        + "\"capacity\":1,\"stock\":0}";

    io.restassured.response.Response resp = given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post("/warehouse");

    ctx.response = resp;
    if (resp.getStatusCode() == 200) {
      // Normalise to uppercase to match the canonical location identifier
      resolvedIdentifier = resp.jsonPath().getString("location").toUpperCase();
      given().when().delete("/warehouse/" + probeCode);
    } else {
      resolvedIdentifier = null;
    }
  }

  @Given("I attempt to resolve the unknown location {string}")
  public void attemptToResolveUnknownLocation(String identifier) {
    pendingUnknownIdentifier = identifier;
  }

  @Then("the location identifier is {string}")
  public void locationIdentifierIs(String expected) {
    assertNotNull(resolvedIdentifier,
        "Location was not resolved — probe warehouse returned status: "
            + (ctx.response != null ? ctx.response.getStatusCode() : "none")
            + " body: " + (ctx.response != null ? ctx.response.getBody().asString() : ""));
    assertEquals(expected.toUpperCase(), resolvedIdentifier,
        "Expected '" + expected + "' but got '" + resolvedIdentifier + "'");
  }

  @Then("a NotFoundException is thrown")
  public void notFoundExceptionIsThrown() {
    String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    String body = "{"
        + "\"businessUnitCode\":\"PRB-" + suffix + "\","
        + "\"location\":\"" + pendingUnknownIdentifier + "\","
        + "\"capacity\":1,\"stock\":0}";

    io.restassured.response.Response resp = given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post("/warehouse");

    assertEquals(404, resp.getStatusCode(),
        "Expected 404 for unknown location '" + pendingUnknownIdentifier
            + "' but got: " + resp.getStatusCode()
            + " body: " + resp.getBody().asString());
  }
}
