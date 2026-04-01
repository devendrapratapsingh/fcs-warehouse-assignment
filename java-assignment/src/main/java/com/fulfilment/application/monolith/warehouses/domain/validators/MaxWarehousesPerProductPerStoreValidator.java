package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.adapters.database.FulfillmentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

/**
 * Rule 1: Each Product can be fulfilled by max 2 Warehouses per Store.
 */
@ApplicationScoped
public class MaxWarehousesPerProductPerStoreValidator {

  private static final int MAX = 2;

  @Inject FulfillmentRepository fulfillmentRepository;

  public void validate(String warehouseCode, Long productId, Long storeId) {
    long count = fulfillmentRepository.countWarehousesForProductInStore(productId, storeId);
    if (count >= MAX) {
      throw new BadRequestException(
          "Product " + productId + " already has " + MAX
              + " warehouses assigned for store " + storeId + ".");
    }
  }
}
