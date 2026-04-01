package com.fulfilment.application.monolith.bdd.steps;

import static io.restassured.RestAssured.given;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.restassured.http.ContentType;
import java.util.Map;

/**
 * Step definitions for warehouse management scenarios.
 *
 * All steps are annotated with @Given only — Cucumber matches any keyword
 * (Given/When/And/Then) to any annotation, so one annotation suffices.
 */
public class WarehouseSteps {

  private final ScenarioContext ctx;

  public WarehouseSteps(ScenarioContext ctx) {
    this.ctx = ctx;
  }

  @Given("the following warehouses exist in the system:")
  public void warehousesExistInSystem(DataTable table) {
    // Seed data is loaded by import.sql — documentation step only.
  }

  @Given("I create a warehouse with:")
  public void createWarehouse(DataTable table) {
    Map<String, String> data = table.asMap(String.class, String.class);
    ctx.response = given()
        .contentType(ContentType.JSON)
        .body(buildWarehouseJson(data))
        .when().post("/warehouse");
  }

  @Given("I replace warehouse {string} with:")
  public void replaceWarehouse(String code, DataTable table) {
    Map<String, String> data = table.asMap(String.class, String.class);
    ctx.response = given()
        .contentType(ContentType.JSON)
        .body(buildWarehouseJson(data))
        .when().post("/warehouse/" + code + "/replacement");
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private String buildWarehouseJson(Map<String, String> data) {
    return "{"
        + "\"businessUnitCode\":\"" + data.get("businessUnitCode") + "\","
        + "\"location\":\"" + data.get("location") + "\","
        + "\"capacity\":" + data.get("capacity") + ","
        + "\"stock\":" + data.get("stock")
        + "}";
  }
}
