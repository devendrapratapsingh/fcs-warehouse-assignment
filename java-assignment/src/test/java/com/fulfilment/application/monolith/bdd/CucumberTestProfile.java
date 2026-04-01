package com.fulfilment.application.monolith.bdd;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * Dedicated Quarkus test profile for the Cucumber BDD runner.
 *
 * By assigning a unique profile, Quarkus Dev Services provisions a SEPARATE
 * PostgreSQL container for CucumberIT. This isolates every mutation made by
 * BDD scenarios (creates/updates/deletes on products, stores, warehouses)
 * from the shared DB used by other @QuarkusTest classes such as
 * ProductResourceTest, StoreResourceTest, and WarehouseEndpointTest.
 *
 * No extra properties are needed — the profile name alone triggers a fresh
 * Dev Services container lifecycle.
 */
public class CucumberTestProfile implements QuarkusTestProfile {

  @Override
  public String getConfigProfile() {
    return "cucumber";
  }
}
