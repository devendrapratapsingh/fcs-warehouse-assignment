package com.fulfilment.application.monolith.fulfillment.domain.ports;

/**
 * Port (driven): read-only fulfillment queries used by domain validators.
 * The domain layer depends on this interface — never on the adapter (FulfillmentRepository).
 */
public interface FulfillmentStore {

  long countWarehousesForProductInStore(Long productId, Long storeId);

  long countDistinctWarehousesForStore(Long storeId);

  long countDistinctProductsInWarehouse(String warehouseBusinessUnitCode);

  boolean exists(String warehouseBusinessUnitCode, Long productId, Long storeId);

  boolean warehouseAlreadyServesStore(String warehouseCode, Long storeId);

  boolean warehouseAlreadyStoresProduct(String warehouseCode, Long productId);
}
