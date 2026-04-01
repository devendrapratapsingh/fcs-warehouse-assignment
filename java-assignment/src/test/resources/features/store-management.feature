Feature: Store Management
  As a retail operations manager
  I want to manage stores (create, list, update, delete)
  So that stores can be associated with warehouses for fulfillment

  Background:
    Given the application is running

  # ── List ──────────────────────────────────────────────────────────────────

  Scenario: List all stores returns seed data
    When I request GET "/store"
    Then the response status is 200
    And the response body contains "TONSTAD"
    And the response body contains "KALLAX"
    And the response body contains "BESTÅ"

  # ── Get by id ─────────────────────────────────────────────────────────────

  Scenario: Get a store by its id
    When I request GET "/store/1"
    Then the response status is 200
    And the response JSON field "name" equals "TONSTAD"

  Scenario: Get a store with unknown id returns 404
    When I request GET "/store/9999"
    Then the response status is 404

  # ── Create ────────────────────────────────────────────────────────────────

  Scenario: Successfully create a new store
    When I POST to "/store" with JSON body:
      """
      { "name": "HEMNES", "quantityProductsInStock": 8 }
      """
    Then the response status is 201
    And the response JSON field "name" equals "HEMNES"
    And the response JSON field "quantityProductsInStock" equals 8

  # ── Update ────────────────────────────────────────────────────────────────

  Scenario: Successfully update an existing store
    When I PUT to "/store/2" with JSON body:
      """
      { "name": "KALLAX-UPDATED", "quantityProductsInStock": 99 }
      """
    Then the response status is 200
    And the response JSON field "name" equals "KALLAX-UPDATED"

  Scenario: Updating a non-existent store returns 404
    When I PUT to "/store/9999" with JSON body:
      """
      { "name": "GHOST", "quantityProductsInStock": 0 }
      """
    Then the response status is 404

  # ── Delete ────────────────────────────────────────────────────────────────

  Scenario: Successfully delete a store
    When I request DELETE "/store/3"
    Then the response status is 204
    And when I request GET "/store/3"
    Then the response status is 404

  Scenario: Deleting a non-existent store returns 404
    When I request DELETE "/store/9999"
    Then the response status is 404
