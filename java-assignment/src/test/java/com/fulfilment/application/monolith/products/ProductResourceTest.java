package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductResourceTest {

  private static final String PATH = "/product";

  // ── GET /product ──────────────────────────────────────────────────────────

  @Test
  @Order(1)
  public void listAllProductsShouldReturnSeedData() {
    given()
        .when().get(PATH)
        .then()
        .statusCode(200)
        .body(containsString("KALLAX"), containsString("BESTÅ"));
  }

  // ── GET /product/{id} ─────────────────────────────────────────────────────

  @Test
  @Order(2)
  public void getSingleProductShouldReturn200() {
    given()
        .when().get(PATH + "/2")
        .then()
        .statusCode(200)
        .body("name", is("KALLAX"));
  }

  @Test
  @Order(3)
  public void getSingleProductNotFoundShouldReturn404() {
    given()
        .when().get(PATH + "/9999")
        .then()
        .statusCode(404);
  }

  // ── POST /product ─────────────────────────────────────────────────────────

  @Test
  @Order(4)
  public void createProductShouldReturn201() {
    String body = """
        {
          "name": "BILLY",
          "description": "Bookcase",
          "price": 59.99,
          "stock": 20
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post(PATH)
        .then()
        .statusCode(201)
        .body("name", is("BILLY"))
        .body("id", notNullValue());
  }

  @Test
  @Order(5)
  public void createProductWithExplicitIdShouldReturn422() {
    String body = """
        {
          "id": 999,
          "name": "SHOULD_FAIL"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post(PATH)
        .then()
        .statusCode(422);
  }

  // ── PUT /product/{id} ─────────────────────────────────────────────────────

  @Test
  @Order(6)
  public void updateProductShouldReturn200() {
    String body = """
        {
          "name": "KALLAX-UPDATED",
          "description": "Updated shelf",
          "price": 79.99,
          "stock": 15
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().put(PATH + "/2")
        .then()
        .statusCode(200)
        .body("name", is("KALLAX-UPDATED"))
        .body("stock", is(15));
  }

  @Test
  @Order(7)
  public void updateProductWithoutNameShouldReturn422() {
    String body = """
        {
          "stock": 5
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().put(PATH + "/2")
        .then()
        .statusCode(422);
  }

  @Test
  @Order(8)
  public void updateNonExistentProductShouldReturn404() {
    String body = """
        {
          "name": "GHOST",
          "stock": 1
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().put(PATH + "/9999")
        .then()
        .statusCode(404);
  }

  // ── DELETE /product/{id} ──────────────────────────────────────────────────

  @Test
  @Order(9)
  public void deleteProductShouldReturn204() {
    // Create a throwaway product then delete it
    String body = """
        {
          "name": "TO-DELETE",
          "stock": 1
        }
        """;

    Integer newId =
        given()
            .contentType(ContentType.JSON)
            .body(body)
            .when().post(PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

    given()
        .when().delete(PATH + "/" + newId)
        .then()
        .statusCode(204);
  }

  @Test
  @Order(10)
  public void deleteNonExistentProductShouldReturn404() {
    given()
        .when().delete(PATH + "/9999")
        .then()
        .statusCode(404);
  }
}
