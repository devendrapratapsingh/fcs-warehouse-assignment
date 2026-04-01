package com.fulfilment.application.monolith.warehouses.adapters.database;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FulfillmentRepository implements PanacheRepository<WarehouseFulfillment> {

  /** How many distinct warehouses serve a given store? (Rule 2: max 3) */
  public long countDistinctWarehousesForStore(Long storeId) {
    return find("storeId = ?1", storeId)
        .stream()
        .map(wf -> wf.warehouseBusinessUnitCode)
        .distinct()
        .count();
  }

  /** How many distinct warehouses fulfill a product for a given store? (Rule 1: max 2) */
  public long countWarehousesForProductInStore(Long productId, Long storeId) {
    return find("productId = ?1 and storeId = ?2", productId, storeId)
        .stream()
        .map(wf -> wf.warehouseBusinessUnitCode)
        .distinct()
        .count();
  }

  /** How many distinct product types does a warehouse store? (Rule 3: max 5) */
  public long countDistinctProductsInWarehouse(String warehouseBusinessUnitCode) {
    return find("warehouseBusinessUnitCode = ?1", warehouseBusinessUnitCode)
        .stream()
        .map(wf -> wf.productId)
        .distinct()
        .count();
  }

  /** Check if this exact association already exists */
  public boolean exists(String warehouseBusinessUnitCode, Long productId, Long storeId) {
    return count("warehouseBusinessUnitCode = ?1 and productId = ?2 and storeId = ?3",
        warehouseBusinessUnitCode, productId, storeId) > 0;
  }

  /** Check whether a given warehouse already serves a given store (any product). */
  public boolean warehouseAlreadyServesStore(String warehouseCode, Long storeId) {
    return find("warehouseBusinessUnitCode = ?1 and storeId = ?2", warehouseCode, storeId)
        .count() > 0;
  }

  /** Check whether a given warehouse already stores a given product (any store). */
  public boolean warehouseAlreadyStoresProduct(String warehouseCode, Long productId) {
    return find("warehouseBusinessUnitCode = ?1 and productId = ?2", warehouseCode, productId)
        .count() > 0;
  }

  public List<WarehouseFulfillment> findByWarehouse(String warehouseBusinessUnitCode) {
    return list("warehouseBusinessUnitCode = ?1", warehouseBusinessUnitCode);
  }
}
