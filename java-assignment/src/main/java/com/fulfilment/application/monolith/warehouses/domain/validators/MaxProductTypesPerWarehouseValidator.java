package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.adapters.database.FulfillmentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

/**
 * Rule 3: Each Warehouse can store max 5 types of Products.
 */
@ApplicationScoped
public class MaxProductTypesPerWarehouseValidator {

  private static final int MAX = 5;

  @Inject FulfillmentRepository fulfillmentRepository;

  public void validate(String warehouseCode, Long productId) {
    // Only count if this product type is new for this warehouse
    if (!fulfillmentRepository.warehouseAlreadyStoresProduct(warehouseCode, productId)) {
      long count = fulfillmentRepository.countDistinctProductsInWarehouse(warehouseCode);
      if (count >= MAX) {
        throw new BadRequestException(
            "Warehouse '" + warehouseCode + "' already stores " + MAX + " product types.");
      }
    }
  }
}
