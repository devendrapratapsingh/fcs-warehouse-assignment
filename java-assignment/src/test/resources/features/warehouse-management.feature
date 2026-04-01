Feature: Warehouse Management
  As a warehouse operations manager
  I want to manage warehouse units (create, view, archive, replace)
  So that the warehouse inventory accurately reflects physical reality

  Background:
    Given the application is running
    And the following warehouses exist in the system:
      | businessUnitCode | location      | capacity | stock |
      | MWH.001          | ZWOLLE-001    | 100      | 10    |
      | MWH.012          | AMSTERDAM-001 | 50       | 5     |
      | MWH.023          | TILBURG-001   | 30       | 27    |

  # ── List ──────────────────────────────────────────────────────────────────

  Scenario: List all active warehouses returns seed data
    When I request GET "/warehouse"
    Then the response status is 200
    And the response body contains "MWH.001"
    And the response body contains "MWH.012"
    And the response body contains "MWH.023"

  # ── Get by code ───────────────────────────────────────────────────────────

  Scenario: Get a warehouse by its business unit code
    When I request GET "/warehouse/MWH.001"
    Then the response status is 200
    And the response JSON field "businessUnitCode" equals "MWH.001"
    And the response JSON field "location" equals "ZWOLLE-001"
    And the response JSON field "capacity" equals 100

  Scenario: Get a warehouse with an unknown code returns 404
    When I request GET "/warehouse/DOES-NOT-EXIST"
    Then the response status is 404

  # ── Create ────────────────────────────────────────────────────────────────

  Scenario: Successfully create a new warehouse at a valid location
    When I create a warehouse with:
      | businessUnitCode | MWH.NEW       |
      | location         | AMSTERDAM-001 |
      | capacity         | 5             |
      | stock            | 0             |
    Then the response status is 200
    And the response JSON field "businessUnitCode" equals "MWH.NEW"
    And the response JSON field "location" equals "AMSTERDAM-001"

  Scenario: Creating a warehouse with a duplicate business unit code is rejected
    When I create a warehouse with:
      | businessUnitCode | MWH.001       |
      | location         | AMSTERDAM-001 |
      | capacity         | 5             |
      | stock            | 0             |
    Then the response status is 400

  Scenario: Creating a warehouse at an unknown location is rejected with 404
    When I create a warehouse with:
      | businessUnitCode | MWH.XYZ    |
      | location         | NOWHERE-999 |
      | capacity         | 20          |
      | stock            | 0           |
    Then the response status is 404

  Scenario: Creating a warehouse whose capacity would exceed the location max capacity is rejected
    # ZWOLLE-001 maxCapacity=40, MWH.001 already uses capacity=100 (exceeds limit)
    # Any new warehouse at ZWOLLE-001 would push total further over max
    When I create a warehouse with:
      | businessUnitCode | MWH.CAP   |
      | location         | ZWOLLE-001 |
      | capacity         | 1          |
      | stock            | 0          |
    Then the response status is 400

  Scenario: Creating more warehouses than a location allows is rejected
    # VETSBY-001 allows maxWarehouses=1 and already has none → first is fine, second should fail
    Given I create a warehouse with:
      | businessUnitCode | MWH.V1    |
      | location         | VETSBY-001 |
      | capacity         | 10         |
      | stock            | 0          |
    And the response status is 200
    When I create a warehouse with:
      | businessUnitCode | MWH.V2    |
      | location         | VETSBY-001 |
      | capacity         | 10         |
      | stock            | 0          |
    Then the response status is 400

  # ── Archive (soft-delete) ─────────────────────────────────────────────────

  Scenario: Archiving a warehouse removes it from the active list
    When I request DELETE "/warehouse/MWH.023"
    Then the response status is 204
    And when I request GET "/warehouse"
    Then the response body does not contain "MWH.023"

  Scenario: Archiving a non-existent warehouse returns 404
    When I request DELETE "/warehouse/DOES-NOT-EXIST"
    Then the response status is 404

  # ── Replace (POST /{code}/replacement) ───────────────────────────────────

  Scenario: Replacing a warehouse updates its details and returns the updated resource
    When I replace warehouse "MWH.012" with:
      | businessUnitCode | MWH.012       |
      | location         | AMSTERDAM-001 |
      | capacity         | 60            |
      | stock            | 5             |
    Then the response status is 200
    And the response JSON field "businessUnitCode" equals "MWH.012"

  Scenario: Replacing a non-existent warehouse returns 404
    When I replace warehouse "DOES-NOT-EXIST" with:
      | businessUnitCode | DOES-NOT-EXIST |
      | location         | AMSTERDAM-001  |
      | capacity         | 50             |
      | stock            | 0              |
    Then the response status is 404
