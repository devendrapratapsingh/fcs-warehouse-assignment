Feature: Location Resolution
  As a warehouse manager
  I want to look up locations by their identifier
  So that warehouses can be assigned to valid physical locations

  Background:
    Given the application is running

  # ── Happy path — use locations with room for probe warehouses ─────────────
  # AMSTERDAM-001: maxWarehouses=5, currently has 1 (MWH.012) → lots of room
  # EINDHOVEN-001: maxWarehouses=2, currently has 0 → room for probes
  # HELMOND-001:   maxWarehouses=1, currently has 0 → room for 1 probe

  Scenario: Resolving an existing location returns the correct identifier
    When I resolve location "AMSTERDAM-001"
    Then the location identifier is "AMSTERDAM-001"

  Scenario: Location resolution is case-insensitive
    When I resolve location "amsterdam-001"
    Then the location identifier is "AMSTERDAM-001"

  Scenario: Another known location is resolvable
    When I resolve location "ZWOLLE-002"
    Then the location identifier is "ZWOLLE-002"

  # ── Not-found guard ───────────────────────────────────────────────────────

  Scenario: Resolving an unknown location throws NotFoundException
    When I attempt to resolve the unknown location "NOWHERE-999"
    Then a NotFoundException is thrown

  # ── Used during warehouse creation ───────────────────────────────────────

  Scenario: Creating a warehouse at a known location succeeds
    When I create a warehouse with:
      | businessUnitCode | MWH.LOC1      |
      | location         | AMSTERDAM-001 |
      | capacity         | 5             |
      | stock            | 0             |
    Then the response status is 200

  Scenario: Creating a warehouse at an unknown location returns 404
    When I create a warehouse with:
      | businessUnitCode | MWH.LOC2    |
      | location         | NOWHERE-999 |
      | capacity         | 10          |
      | stock            | 0           |
    Then the response status is 404
