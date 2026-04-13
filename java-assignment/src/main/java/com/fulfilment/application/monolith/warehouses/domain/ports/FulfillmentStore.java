package com.fulfilment.application.monolith.warehouses.domain.ports;

/**
 * Port interface for fulfillment read operations.
 * Domain validators depend on this port instead of the adapter (FulfillmentRepository).
 */
public interface FulfillmentStore {

  long countWarehousesForProductInStore(Long productId, Long storeId);

  long countDistinctWarehousesForStore(Long storeId);

  long countDistinctProductsInWarehouse(String warehouseBusinessUnitCode);

  boolean exists(String warehouseBusinessUnitCode, Long productId, Long storeId);

  boolean warehouseAlreadyServesStore(String warehouseCode, Long storeId);

  boolean warehouseAlreadyStoresProduct(String warehouseCode, Long productId);
}
