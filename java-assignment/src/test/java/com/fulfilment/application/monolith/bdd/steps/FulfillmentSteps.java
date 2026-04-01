package com.fulfilment.application.monolith.bdd.steps;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * Step definitions for warehouse fulfillment association scenarios.
 *
 * All steps are annotated with @Given only.  In Cucumber, the keyword used in a
 * feature file (Given / When / And / Then) is purely cosmetic — any annotation
 * will match any keyword, so a single @Given annotation handles all usages.
 */
public class FulfillmentSteps {

  private final ScenarioContext ctx;

  public FulfillmentSteps(ScenarioContext ctx) {
    this.ctx = ctx;
  }

  @Given("the seed fulfillment data exists:")
  public void seedFulfillmentDataExists(DataTable table) {
    // Seed is loaded by import.sql — documentation step only.
  }

  @Given("I add fulfillment to warehouse {string} with productId {long} and storeId {long}")
  public void addFulfillment(String warehouseCode, long productId, long storeId) {
    String body = "{ \"productId\": " + productId + ", \"storeId\": " + storeId + " }";
    ctx.response = given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post("/warehouse/" + warehouseCode + "/fulfillment");
  }

  @Given("I add fulfillment to warehouse {string} with stored product {string} and storeId {long}")
  public void addFulfillmentWithNamedProduct(String warehouseCode, String alias, long storeId) {
    Long productId = ctx.namedIds.get(alias);
    assertNotNull(productId, "No product stored under alias '" + alias + "'.");
    addFulfillment(warehouseCode, productId, storeId);
  }

  @Given("I create a product named {string} with stock {int} and store it as {string}")
  public void createProductAndStoreAlias(String name, int stock, String alias) {
    String body = "{ \"name\": \"" + name + "\", \"stock\": " + stock + " }";
    Response resp = given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post("/product");
    ctx.response = resp;
    ctx.namedIds.put(alias, resp.jsonPath().getLong("id"));
  }

  @Given("I create a store named {string} with stock {int} and store it as {string}")
  public void createStoreAndStoreAlias(String name, int stock, String alias) {
    String body = "{ \"name\": \"" + name + "\", \"quantityProductsInStock\": " + stock + " }";
    Response resp = given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post("/store");
    ctx.response = resp;
    ctx.namedIds.put(alias, resp.jsonPath().getLong("id"));
  }

  @Given("I add fulfillment to warehouse {string} with stored product {string} and stored store {string}")
  public void addFulfillmentWithNamedProductAndStore(String warehouseCode, String productAlias, String storeAlias) {
    Long productId = ctx.namedIds.get(productAlias);
    assertNotNull(productId, "No product stored under alias '" + productAlias + "'.");
    Long storeId = ctx.namedIds.get(storeAlias);
    assertNotNull(storeId, "No store stored under alias '" + storeAlias + "'.");
    addFulfillment(warehouseCode, productId, storeId);
  }
}
