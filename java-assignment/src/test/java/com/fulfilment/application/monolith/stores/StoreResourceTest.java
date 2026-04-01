package com.fulfilment.application.monolith.stores;

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
public class StoreResourceTest {

  private static final String PATH = "/store";

  // ── GET /store ────────────────────────────────────────────────────────────

  @Test
  @Order(1)
  public void listAllStoresShouldReturnSeedData() {
    given()
        .when().get(PATH)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));
  }

  // ── GET /store/{id} ───────────────────────────────────────────────────────

  @Test
  @Order(2)
  public void getSingleStoreShouldReturn200() {
    given()
        .when().get(PATH + "/2")
        .then()
        .statusCode(200)
        .body("name", is("KALLAX"));
  }

  @Test
  @Order(3)
  public void getSingleStoreNotFoundShouldReturn404() {
    given()
        .when().get(PATH + "/9999")
        .then()
        .statusCode(404);
  }

  // ── POST /store ───────────────────────────────────────────────────────────

  @Test
  @Order(4)
  public void createStoreShouldReturn201() {
    String body = """
        {
          "name": "STOCKHOLM",
          "quantityProductsInStock": 7
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().post(PATH)
        .then()
        .statusCode(201)
        .body("name", is("STOCKHOLM"))
        .body("id", notNullValue());
  }

  @Test
  @Order(5)
  public void createStoreWithExplicitIdShouldReturn422() {
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

  // ── PUT /store/{id} ───────────────────────────────────────────────────────

  @Test
  @Order(6)
  public void updateStoreShouldReturn200() {
    String body = """
        {
          "name": "KALLAX-UPDATED",
          "quantityProductsInStock": 99
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().put(PATH + "/2")
        .then()
        .statusCode(200)
        .body("name", is("KALLAX-UPDATED"))
        .body("quantityProductsInStock", is(99));
  }

  @Test
  @Order(7)
  public void updateStoreWithoutNameShouldReturn422() {
    String body = """
        {
          "quantityProductsInStock": 5
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
  public void updateNonExistentStoreShouldReturn404() {
    String body = """
        {
          "name": "GHOST",
          "quantityProductsInStock": 1
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().put(PATH + "/9999")
        .then()
        .statusCode(404);
  }

  // ── PATCH /store/{id} ─────────────────────────────────────────────────────

  @Test
  @Order(9)
  public void patchStoreShouldReturn200() {
    String body = """
        {
          "name": "BESTÅ-PATCHED",
          "quantityProductsInStock": 10
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().patch(PATH + "/3")
        .then()
        .statusCode(200)
        .body("name", is("BESTÅ-PATCHED"));
  }

  @Test
  @Order(10)
  public void patchStoreWithoutNameShouldReturn422() {
    String body = """
        {
          "quantityProductsInStock": 5
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().patch(PATH + "/3")
        .then()
        .statusCode(422);
  }

  @Test
  @Order(11)
  public void patchNonExistentStoreShouldReturn404() {
    String body = """
        {
          "name": "GHOST",
          "quantityProductsInStock": 1
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when().patch(PATH + "/9999")
        .then()
        .statusCode(404);
  }

  // ── DELETE /store/{id} ────────────────────────────────────────────────────

  @Test
  @Order(12)
  public void deleteStoreShouldReturn204() {
    // Create a throwaway store to delete
    String body = """
        {
          "name": "TO-DELETE",
          "quantityProductsInStock": 1
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

    // Confirm it's gone
    given()
        .when().get(PATH + "/" + newId)
        .then()
        .statusCode(404);
  }

  @Test
  @Order(13)
  public void deleteNonExistentStoreShouldReturn404() {
    given()
        .when().delete(PATH + "/9999")
        .then()
        .statusCode(404);
  }
}
