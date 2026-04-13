package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.ports.FulfillmentStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

/**
 * Rule 1: Each Product can be fulfilled by max 2 Warehouses per Store.
 */
@ApplicationScoped
public class MaxWarehousesPerProductPerStoreValidator {

  private static final int MAX = 2;

  private final FulfillmentStore fulfillmentStore;

  public MaxWarehousesPerProductPerStoreValidator(FulfillmentStore fulfillmentStore) {
    this.fulfillmentStore = fulfillmentStore;
  }

  public void validate(String warehouseCode, Long productId, Long storeId) {
    long count = fulfillmentStore.countWarehousesForProductInStore(productId, storeId);
    if (count >= MAX) {
      throw new BadRequestException(
          "Product " + productId + " already has " + MAX
              + " warehouses assigned for store " + storeId + ".");
    }
  }
}
