package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.adapters.database.FulfillmentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

/**
 * Rule 2: Each Store can be fulfilled by max 3 Warehouses.
 */
@ApplicationScoped
public class MaxWarehousesPerStoreValidator {

  private static final int MAX = 3;

  @Inject FulfillmentRepository fulfillmentRepository;

  public void validate(String warehouseCode, Long storeId) {
    // Only count distinct warehouses if this warehouse is not already serving this store
    if (!fulfillmentRepository.warehouseAlreadyServesStore(warehouseCode, storeId)) {
      long count = fulfillmentRepository.countDistinctWarehousesForStore(storeId);
      if (count >= MAX) {
        throw new BadRequestException(
            "Store " + storeId + " already has " + MAX + " warehouses assigned.");
      }
    }
  }
}
