package com.fulfilment.application.monolith.bdd.steps;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.http.ContentType;

/**
 * Reusable HTTP and assertion step definitions shared across all feature files.
 *
 * All steps use @Given — Cucumber treats Given/When/And/Then keywords as cosmetic;
 * a single @Given annotation matches all of them, avoiding duplicate-registration errors.
 */
public class CommonSteps {

  private final ScenarioContext ctx;

  public CommonSteps(ScenarioContext ctx) {
    this.ctx = ctx;
  }

  // ── Application readiness ────────────────────────────────────────────────

  @Given("the application is running")
  public void applicationIsRunning() {
    // Quarkus is already booted — nothing to do.
  }

  // ── HTTP verbs ───────────────────────────────────────────────────────────

  @Given("I request GET {string}")
  public void requestGet(String path) {
    ctx.response = given().when().get(path);
  }

  @Given("when I request GET {string}")
  public void andRequestGet(String path) {
    ctx.response = given().when().get(path);
  }

  @Given("I request DELETE {string}")
  public void requestDelete(String path) {
    ctx.response = given().when().delete(path);
  }

  @Given("I POST to {string} with JSON body:")
  public void postWithBody(String path, String body) {
    ctx.response = given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post(path);
  }

  @Given("I PUT to {string} with JSON body:")
  public void putWithBody(String path, String body) {
    ctx.response = given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().put(path);
  }

  // ── Status assertions ────────────────────────────────────────────────────

  @Then("the response status is {int}")
  public void responseStatusIs(int expected) {
    assertNotNull(ctx.response, "No HTTP response captured — check your step.");
    assertEquals(expected, ctx.response.getStatusCode(),
        "Unexpected HTTP status. Body: " + ctx.response.getBody().asString());
  }

  // ── Body assertions ───────────────────────────────────────────────────────

  @Then("the response body contains {string}")
  public void responseBodyContains(String text) {
    assertThat(ctx.response.getBody().asString(), containsString(text));
  }

  @Then("the response body does not contain {string}")
  public void responseBodyDoesNotContain(String text) {
    assertThat(ctx.response.getBody().asString(), not(containsString(text)));
  }

  @Then("the response body is not empty")
  public void responseBodyNotEmpty() {
    assertThat(ctx.response.getBody().asString(), notNullValue());
  }

  // ── JSON field assertions ─────────────────────────────────────────────────

  @Then("the response JSON field {string} equals {string}")
  public void responseJsonFieldEqualsString(String field, String expected) {
    String actual = ctx.response.jsonPath().getString(field);
    assertEquals(expected, actual,
        "Field '" + field + "' mismatch. Body: " + ctx.response.getBody().asString());
  }

  @Then("the response JSON field {string} equals {int}")
  public void responseJsonFieldEqualsInt(String field, int expected) {
    int actual = ctx.response.jsonPath().getInt(field);
    assertEquals(expected, actual,
        "Field '" + field + "' mismatch. Body: " + ctx.response.getBody().asString());
  }
}
