Feature: Warehouse Fulfillment Association
  As a logistics coordinator
  I want to associate products and stores with warehouses
  So that I know which warehouse fulfils which products for which stores

  Background:
    Given the application is running
    And the seed fulfillment data exists:
      | warehouseCode | productId | storeId |
      | MWH.001       | 1         | 1       |
      | MWH.001       | 2         | 1       |

  # ── Happy path ────────────────────────────────────────────────────────────

  Scenario: Successfully add a new fulfillment association
    # Create a fresh product so we are not affected by other scenario ordering
    Given I create a product named "PROD-FULFILL-A" with stock 1 and store it as "pfa"
    When I add fulfillment to warehouse "MWH.001" with stored product "pfa" and storeId 1
    Then the response status is 201
    And the response body is not empty

  Scenario: Successfully add a fulfillment for a different store
    Given I create a store named "STORE-WF" with stock 1 and store it as "wfStore"
    And I create a product named "PROD-FULFILL-B" with stock 1 and store it as "pfb"
    When I add fulfillment to warehouse "MWH.012" with stored product "pfb" and stored store "wfStore"
    Then the response status is 201

  # ── Duplicate guard ───────────────────────────────────────────────────────

  Scenario: Adding a duplicate fulfillment association is rejected
    # MWH.001 + product2 + store1 always exists in seed data
    When I add fulfillment to warehouse "MWH.001" with productId 2 and storeId 1
    Then the response status is 400

  # ── Not-found guards ──────────────────────────────────────────────────────

  Scenario: Adding a fulfillment for an unknown warehouse returns 404
    When I add fulfillment to warehouse "DOES-NOT-EXIST" with productId 1 and storeId 1
    Then the response status is 404

  Scenario: Adding a fulfillment for an unknown store returns 404
    When I add fulfillment to warehouse "MWH.001" with productId 1 and storeId 9999
    Then the response status is 404

  Scenario: Adding a fulfillment for an unknown product returns 404
    When I add fulfillment to warehouse "MWH.001" with productId 9999 and storeId 1
    Then the response status is 404
