package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Integration tests for the Warehouse REST API.
 *
 * Uses @QuarkusTest — boots the full application with Quarkus Dev Services
 * (auto-starts a Postgres container via Docker). No external DB needed.
 *
 * Run with: ./mvnw test
 *
 * Contrast with WarehouseEndpointIT (@QuarkusIntegrationTest) which tests
 * the fully packaged JAR and requires: ./mvnw verify
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseEndpointTest {

  private static final String PATH = "/warehouse";

  // ── GET /warehouse ────────────────────────────────────────────────────────

  @Test
  @Order(1)
  public void listAllWarehousesShouldReturnSeedData() {
    given()
        .when().get(PATH)
        .then()
        .statusCode(200)
        .body(
            containsString("MWH.001"),
            containsString("MWH.012"),
            containsString("MWH.023"));
  }

  // ── GET /warehouse/{id} ───────────────────────────────────────────────────

  @Test
  @Order(2)
  public void getWarehouseByIdShouldReturn200() {
    // id=1 is MWH.001 (ZWOLLE-001, capacity=100) from seed data
    given()
        .when().get(PATH + "/1")
        .then()
        .statusCode(200)
        .body("businessUnitCode", is("MWH.001"))
        .body("location", is("ZWOLLE-001"))
        .body("capacity", is(100));
  }

  @Test
  @Order(3)
  public void getWarehouseByAnotherIdShouldReturn200() {
    // id=2 is MWH.012 (AMSTERDAM-001, capacity=50) from seed data
    // MUST run before Order 13 (replace) which archives id=2
    given()
        .when().get(PATH + "/2")
        .then()
        .statusCode(200)
        .body("businessUnitCode", is("MWH.012"))
        .body("location", is("AMSTERDAM-001"))
        .body("capacity", is(50));
  }

  @Test
  @Order(4)
  public void getWarehouseByUnknownIdShouldReturn404() {
    given()
        .when().get(PATH + "/9999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(5)
  public void getWarehouseByNonNumericIdShouldReturn404() {
    given()
        .when().get(PATH + "/DOES-NOT-EXIST")
        .then()
        .statusCode(404);
  }

  // ── POST /warehouse ───────────────────────────────────────────────────────

  @Test
  @Order(6)
  public void createWarehouseShouldReturn200AndPersist() {
    // AMSTERDAM-001 allows 5 warehouses, has 1 (MWH.012 cap=50), maxCapacity=100
    // new cap=20 → total 70 < 100 ✓
    String body = """
        {
          "businessUnitCode": "MWH.999",
          "location": "AMSTERDAM-001",
          "capacity": 20,
          "stock": 0
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post(PATH)
        .then()
        .statusCode(200)
        .body("businessUnitCode", is("MWH.999"))
        .body("location", is("AMSTERDAM-001"));

    // Confirm it appears in the list
    given()
        .when().get(PATH)
        .then()
        .statusCode(200)
        .body(containsString("MWH.999"));
  }

  @Test
  @Order(7)
  public void createWarehouseWithDuplicateCodeShouldReturn400() {
    // MWH.001 already exists — duplicate businessUnitCode must be rejected
    String body = """
        {
          "businessUnitCode": "MWH.001",
          "location": "AMSTERDAM-001",
          "capacity": 5,
          "stock": 0
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @Order(8)
  public void createWarehouseWithUnknownLocationShouldReturn404() {
    String body = """
        {
          "businessUnitCode": "MWH.777",
          "location": "NOWHERE-999",
          "capacity": 20,
          "stock": 0
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post(PATH)
        .then()
        .statusCode(404);
  }

  // ── DELETE /warehouse/{id}  (archive) ─────────────────────────────────────

  @Test
  @Order(9)
  public void archiveWarehouseShouldReturn204AndDisappearFromList() {
    // Archive MWH.023 (id=3)
    given()
        .when().delete(PATH + "/3")
        .then()
        .statusCode(204);

    // No longer in active list
    given()
        .when().get(PATH)
        .then()
        .statusCode(200)
        .body(not(containsString("MWH.023")));
  }

  @Test
  @Order(10)
  public void archiveNonExistentWarehouseShouldReturn404() {
    given()
        .when().delete(PATH + "/9999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(11)
  public void archiveNonNumericIdShouldReturn404() {
    given()
        .when().delete(PATH + "/DOES-NOT-EXIST")
        .then()
        .statusCode(404);
  }

  // ── POST /warehouse/{id}/fulfillment ──────────────────────────────────────

  @Test
  @Order(12)
  public void addFulfillmentShouldReturn201() {
    // MWH.001 + product 3 + store 1 — not yet associated (seed data has 1+2)
    String body = """
        {
          "productId": 3,
          "storeId": 1
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post(PATH + "/MWH.001/fulfillment")
        .then()
        .statusCode(201)
        .body(notNullValue());
  }

  @Test
  @Order(13)
  public void addDuplicateFulfillmentShouldReturn400() {
    // MWH.001 + product 2 + store 1 already exists in seed data (id=2 is KALLAX, never deleted)
    String body = """
        {
          "productId": 2,
          "storeId": 1
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post(PATH + "/MWH.001/fulfillment")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(14)
  public void addFulfillmentForUnknownWarehouseShouldReturn404() {
    String body = """
        { "productId": 2, "storeId": 1 }
        """;
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post(PATH + "/DOES-NOT-EXIST/fulfillment")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(15)
  public void addFulfillmentForUnknownStoreShouldReturn404() {
    String body = """
        { "productId": 2, "storeId": 9999 }
        """;
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post(PATH + "/MWH.001/fulfillment")
        .then()
        .statusCode(404);
  }

  // ── POST /warehouse/{id}/replacement (replace) ───────────────────────────

  @Test
  @Order(16)
  public void replaceWarehouseShouldReturn200() {
    // MWH.012 (AMSTERDAM-001, cap=50, stock=5) → replace with higher capacity
    String body = """
        {
          "businessUnitCode": "MWH.012",
          "location": "AMSTERDAM-001",
          "capacity": 60,
          "stock": 5
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post(PATH + "/MWH.012/replacement")
        .then()
        .statusCode(200)
        .body("businessUnitCode", is("MWH.012"));
  }

  @Test
  @Order(17)
  public void replaceNonExistentWarehouseShouldReturn404() {
    String body = """
        {
          "businessUnitCode": "DOES-NOT-EXIST",
          "location": "AMSTERDAM-001",
          "capacity": 50,
          "stock": 0
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post(PATH + "/DOES-NOT-EXIST/replacement")
        .then()
        .statusCode(404);
  }

  // ── Fulfillment business rules ─────────────────────────────────────────────

  @Test
  @Order(18)
  public void addFulfillmentExceedingMaxWarehousesPerProductPerStoreShouldReturn400() {
    // seed: MWH.001→product2→store1, MWH.012→product2→store1 would be the 2nd → ok
    // MWH.001→product2→store1 already exists; add MWH.012→product2→store1 = 2nd = ok
    // then add MWH.999→product2→store1 = 3rd = FAIL (max 2)

    // Step 1: add MWH.012 → product2 → store1 (this is the 2nd, allowed)
    String body1 = """
        { "productId": 2, "storeId": 1 }
        """;
    given()
        .contentType(ContentType.JSON)
        .body(body1)
        .when().post(PATH + "/MWH.012/fulfillment")
        .then()
        .statusCode(201);

    // Step 2: add MWH.999 → product2 → store1 (3rd warehouse for same product+store = FAIL)
    String body2 = """
        { "productId": 2, "storeId": 1 }
        """;
    given()
        .contentType(ContentType.JSON)
        .body(body2)
        .when().post(PATH + "/MWH.999/fulfillment")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(19)
  public void addFulfillmentExceedingMaxProductTypesPerWarehouseShouldReturn400() {
    // Fill MWH.001 with 5 product types then try a 6th
    // seed already has product1+product2+product3 for MWH.001 (3 types)
    // add product types via store2 to avoid duplicate conflicts

    // product 3 → store2
    given().contentType(ContentType.JSON)
        .body("""
            { "productId": 3, "storeId": 2 }
            """)
        .when().post(PATH + "/MWH.001/fulfillment").then().statusCode(201);

    // Now create 2 more products to add
    Integer p4id = given().contentType(ContentType.JSON)
        .body("""
            { "name": "PRODUCT-D", "stock": 1 }
            """)
        .when().post("/product").then().statusCode(201).extract().path("id");

    Integer p5id = given().contentType(ContentType.JSON)
        .body("""
            { "name": "PRODUCT-E", "stock": 1 }
            """)
        .when().post("/product").then().statusCode(201).extract().path("id");

    Integer p6id = given().contentType(ContentType.JSON)
        .body("""
            { "name": "PRODUCT-F", "stock": 1 }
            """)
        .when().post("/product").then().statusCode(201).extract().path("id");

    // 4th product type in MWH.001
    given().contentType(ContentType.JSON)
        .body("{ \"productId\": " + p4id + ", \"storeId\": 2 }")
        .when().post(PATH + "/MWH.001/fulfillment").then().statusCode(201);

    // 5th product type in MWH.001
    given().contentType(ContentType.JSON)
        .body("{ \"productId\": " + p5id + ", \"storeId\": 2 }")
        .when().post(PATH + "/MWH.001/fulfillment").then().statusCode(201);

    // 6th product type → must fail (max 5)
    given().contentType(ContentType.JSON)
        .body("{ \"productId\": " + p6id + ", \"storeId\": 2 }")
        .when().post(PATH + "/MWH.001/fulfillment").then().statusCode(400);
  }

  @Test
  @Order(20)
  public void addFulfillmentExceedingMaxWarehousesPerStoreShouldReturn400() {
    // store2 currently served by MWH.001 (from order 16 above)
    // add MWH.012 → store2 = 2nd warehouse for store2 (ok)
    given().contentType(ContentType.JSON)
        .body("""
            { "productId": 3, "storeId": 2 }
            """)
        .when().post(PATH + "/MWH.012/fulfillment")
        // already exists from order 15 path, might be 201 or 400 — either way continue
        .then().statusCode(anyOf(201, 400));

    // create 2 more warehouses to reach the limit of 3 distinct warehouses per store
    String wh1 = """
        {
          "businessUnitCode": "MWH.STR1",
          "location": "VETSBY-001",
          "capacity": 50,
          "stock": 0
        }
        """;
    String wh2 = """
        {
          "businessUnitCode": "MWH.STR2",
          "location": "EINDHOVEN-001",
          "capacity": 50,
          "stock": 0
        }
        """;

    given().contentType(ContentType.JSON).body(wh1).when().post(PATH).then().statusCode(200);
    given().contentType(ContentType.JSON).body(wh2).when().post(PATH).then().statusCode(200);

    Integer pNew = given().contentType(ContentType.JSON)
        .body("""
            { "name": "PRODUCT-G", "stock": 1 }
            """)
        .when().post("/product").then().statusCode(201).extract().path("id");

    // 2nd warehouse for store3
    given().contentType(ContentType.JSON)
        .body("{ \"productId\": " + pNew + ", \"storeId\": 3 }")
        .when().post(PATH + "/MWH.STR1/fulfillment").then().statusCode(201);

    // 3rd warehouse for store3
    given().contentType(ContentType.JSON)
        .body("{ \"productId\": " + pNew + ", \"storeId\": 3 }")
        .when().post(PATH + "/MWH.STR2/fulfillment").then().statusCode(201);

    // 4th warehouse for store3 → must fail (max 3)
    given().contentType(ContentType.JSON)
        .body("{ \"productId\": " + pNew + ", \"storeId\": 3 }")
        .when().post(PATH + "/MWH.999/fulfillment").then().statusCode(400);
  }

  // helper — Hamcrest anyOf for int status
  private static org.hamcrest.Matcher<Integer> anyOf(int a, int b) {
    return org.hamcrest.Matchers.anyOf(
        org.hamcrest.Matchers.is(a), org.hamcrest.Matchers.is(b));
  }
}
