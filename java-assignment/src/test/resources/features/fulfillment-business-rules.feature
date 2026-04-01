Feature: Fulfillment Business Rules
  As a supply chain manager
  I want the system to enforce capacity and distribution rules on fulfillment
  So that no single warehouse, store or product exceeds its allowed limits

  Background:
    Given the application is running

  # ── Rule 1: max 2 warehouses per product per store ────────────────────────
  # Use product1+store3 (no seed fulfillments for store3).

  Scenario: A product can be fulfilled by at most 2 warehouses per store
    Given I add fulfillment to warehouse "MWH.001" with productId 1 and storeId 3
    And the response status is 201
    Given I add fulfillment to warehouse "MWH.012" with productId 1 and storeId 3
    And the response status is 201
    When I add fulfillment to warehouse "MWH.023" with productId 1 and storeId 3
    Then the response status is 400

  Scenario: First two warehouses for the same product and store are both accepted
    Given I add fulfillment to warehouse "MWH.001" with productId 2 and storeId 3
    Then the response status is 201
    When I add fulfillment to warehouse "MWH.012" with productId 2 and storeId 3
    Then the response status is 201

  # ── Rule 2: max 3 warehouses per store ───────────────────────────────────
  # IMPORTANT: Each warehouse uses a DIFFERENT product to avoid hitting the
  # "max 2 warehouses per product per store" rule at the same time.
  # Use store 2 (KALLAX) which starts with 0 fulfillments in seed.

  Scenario: A store can be served by at most 3 distinct warehouses
    Given I create a warehouse with:
      | businessUnitCode | MWH.S2A       |
      | location         | AMSTERDAM-002 |
      | capacity         | 5             |
      | stock            | 0             |
    And I create a warehouse with:
      | businessUnitCode | MWH.S2B       |
      | location         | AMSTERDAM-002 |
      | capacity         | 5             |
      | stock            | 0             |
    And I create a warehouse with:
      | businessUnitCode | MWH.S2C       |
      | location         | AMSTERDAM-002 |
      | capacity         | 5             |
      | stock            | 0             |
    And I create a warehouse with:
      | businessUnitCode | MWH.S2D       |
      | location         | EINDHOVEN-001 |
      | capacity         | 5             |
      | stock            | 0             |
    # Each warehouse uses a different product to avoid Rule 1 conflicts
    Given I add fulfillment to warehouse "MWH.S2A" with productId 1 and storeId 2
    Then the response status is 201
    Given I add fulfillment to warehouse "MWH.S2B" with productId 2 and storeId 2
    Then the response status is 201
    Given I add fulfillment to warehouse "MWH.S2C" with productId 3 and storeId 2
    Then the response status is 201
    # 4th distinct warehouse → rejected (store2 already has 3 distinct warehouses)
    When I add fulfillment to warehouse "MWH.S2D" with productId 1 and storeId 2
    Then the response status is 400

  Scenario: Exactly 3 warehouses per store is the accepted boundary
    Given I create a warehouse with:
      | businessUnitCode | MWH.S2E       |
      | location         | EINDHOVEN-001 |
      | capacity         | 5             |
      | stock            | 0             |
    # Use fresh products (store1 already has MWH.001 from seed via product1+product2)
    Given I create a product named "PROD-BND-A" with stock 1 and store it as "bndA"
    And   I create a product named "PROD-BND-B" with stock 1 and store it as "bndB"
    And   I create a product named "PROD-BND-C" with stock 1 and store it as "bndC"
    # 3 warehouses each with a unique product → all within limits
    Given I add fulfillment to warehouse "MWH.001" with stored product "bndA" and storeId 1
    And   I add fulfillment to warehouse "MWH.012" with stored product "bndB" and storeId 1
    When  I add fulfillment to warehouse "MWH.S2E" with stored product "bndC" and storeId 1
    Then the response status is 201

  # ── Rule 3: max 5 product types per warehouse ─────────────────────────────
  # Use store 3 (BESTÅ) which has 2 distinct warehouses so far (MWH.001 + MWH.012 from Rule-1).
  # MWH.P5A will become the 3rd distinct warehouse for store3 (max=3), so the FIRST
  # fulfillment succeeds, and subsequent ones reuse the same warehouse → no Rule-2 conflict.

  Scenario: A warehouse can hold at most 5 distinct product types
    Given I create a warehouse with:
      | businessUnitCode | MWH.P5A       |
      | location         | AMSTERDAM-001 |
      | capacity         | 5             |
      | stock            | 0             |
    And I create a product named "PROD-P5-A" with stock 1 and store it as "pa"
    And I create a product named "PROD-P5-B" with stock 1 and store it as "pb"
    And I create a product named "PROD-P5-C" with stock 1 and store it as "pc"
    And I create a product named "PROD-P5-D" with stock 1 and store it as "pd"
    And I create a product named "PROD-P5-E" with stock 1 and store it as "pe"
    And I create a product named "PROD-P5-F" with stock 1 and store it as "pf"
    Given I add fulfillment to warehouse "MWH.P5A" with stored product "pa" and storeId 3
    Then the response status is 201
    Given I add fulfillment to warehouse "MWH.P5A" with stored product "pb" and storeId 3
    Then the response status is 201
    Given I add fulfillment to warehouse "MWH.P5A" with stored product "pc" and storeId 3
    Then the response status is 201
    Given I add fulfillment to warehouse "MWH.P5A" with stored product "pd" and storeId 3
    Then the response status is 201
    Given I add fulfillment to warehouse "MWH.P5A" with stored product "pe" and storeId 3
    Then the response status is 201
    When  I add fulfillment to warehouse "MWH.P5A" with stored product "pf" and storeId 3
    Then the response status is 400

  Scenario: Adding the same product type in a different store does not count as a new distinct type
    # product1 is already in MWH.001 via store1 (seed).
    # Adding product1 for store2: only 1 distinct type added → no new type increase.
    # NOTE: store2 now has 3 warehouses after Rule-2 scenario (S2A/B/C). Adding MWH.001
    # would be a 4th distinct warehouse for store2 → hits max 3. Use store3 instead.
    # store3 had: MWH.001+product1+store3 and MWH.012+product1+store3 (from Rule-1).
    # Adding product1 again for store3 on MWH.001 would be duplicate → use store1 for MWH.012.
    # MWH.012 has NOT served store1 yet in any prior scenario → 0 warehouse count for store1.
    # store1 has: MWH.001 (seed). Adding MWH.012→product1→store1 = 2nd distinct warehouse = OK.
    Given I create a product named "PROD-SAME" with stock 1 and store it as "ps"
    When  I add fulfillment to warehouse "MWH.001" with stored product "ps" and storeId 1
    Then the response status is 201
    # Adding same product again for a different store still uses same product type in warehouse
    When  I add fulfillment to warehouse "MWH.001" with stored product "ps" and storeId 3
    Then the response status is 201
