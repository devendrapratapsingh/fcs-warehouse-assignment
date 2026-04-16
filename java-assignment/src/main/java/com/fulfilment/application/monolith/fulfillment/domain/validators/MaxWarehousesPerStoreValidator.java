package com.fulfilment.application.monolith.fulfillment.domain.validators;

import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

/**
 * Rule 2: Each Store can be fulfilled by max 3 Warehouses.
 */
@ApplicationScoped
public class MaxWarehousesPerStoreValidator {

  private static final int MAX = 3;

  private final FulfillmentStore fulfillmentStore;

  public MaxWarehousesPerStoreValidator(FulfillmentStore fulfillmentStore) {
    this.fulfillmentStore = fulfillmentStore;
  }

  public void validate(String warehouseCode, Long storeId) {
    if (!fulfillmentStore.warehouseAlreadyServesStore(warehouseCode, storeId)) {
      long count = fulfillmentStore.countDistinctWarehousesForStore(storeId);
      if (count >= MAX) {
        throw new BadRequestException(
            "Store " + storeId + " already has " + MAX + " warehouses assigned.");
      }
    }
  }
}
