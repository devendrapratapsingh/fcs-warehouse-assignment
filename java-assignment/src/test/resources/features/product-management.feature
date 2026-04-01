Feature: Product Management
  As an inventory manager
  I want to manage products (create, list, update, delete)
  So that products can be tracked and associated with warehouse fulfillments

  Background:
    Given the application is running

  # ── List ──────────────────────────────────────────────────────────────────

  Scenario: List all products returns seed data
    When I request GET "/product"
    Then the response status is 200
    And the response body contains "TONSTAD"
    And the response body contains "KALLAX"
    And the response body contains "BESTÅ"

  # ── Get by id ─────────────────────────────────────────────────────────────

  Scenario: Get a product by its id
    When I request GET "/product/1"
    Then the response status is 200
    And the response JSON field "name" equals "TONSTAD"

  Scenario: Get a product with unknown id returns 404
    When I request GET "/product/9999"
    Then the response status is 404

  # ── Create ────────────────────────────────────────────────────────────────

  Scenario: Successfully create a new product
    When I POST to "/product" with JSON body:
      """
      { "name": "BILLY", "stock": 15 }
      """
    Then the response status is 201
    And the response JSON field "name" equals "BILLY"
    And the response JSON field "stock" equals 15

  # ── Update ────────────────────────────────────────────────────────────────

  Scenario: Successfully update an existing product
    When I PUT to "/product/2" with JSON body:
      """
      { "name": "KALLAX-V2", "stock": 20 }
      """
    Then the response status is 200
    And the response JSON field "name" equals "KALLAX-V2"

  Scenario: Updating a non-existent product returns 404
    When I PUT to "/product/9999" with JSON body:
      """
      { "name": "GHOST", "stock": 0 }
      """
    Then the response status is 404

  # ── Delete ────────────────────────────────────────────────────────────────

  Scenario: Successfully delete a product
    When I request DELETE "/product/3"
    Then the response status is 204
    And when I request GET "/product/3"
    Then the response status is 404

  Scenario: Deleting a non-existent product returns 404
    When I request DELETE "/product/9999"
    Then the response status is 404
