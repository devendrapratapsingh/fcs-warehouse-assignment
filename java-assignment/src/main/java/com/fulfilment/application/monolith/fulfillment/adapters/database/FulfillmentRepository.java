package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FulfillmentRepository implements FulfillmentStore, PanacheRepository<WarehouseFulfillment> {

  @Override
  public long countDistinctWarehousesForStore(Long storeId) {
    return find("storeId = ?1", storeId)
        .stream()
        .map(wf -> wf.warehouseBusinessUnitCode)
        .distinct()
        .count();
  }

  @Override
  public long countWarehousesForProductInStore(Long productId, Long storeId) {
    return find("productId = ?1 and storeId = ?2", productId, storeId)
        .stream()
        .map(wf -> wf.warehouseBusinessUnitCode)
        .distinct()
        .count();
  }

  @Override
  public long countDistinctProductsInWarehouse(String warehouseBusinessUnitCode) {
    return find("warehouseBusinessUnitCode = ?1", warehouseBusinessUnitCode)
        .stream()
        .map(wf -> wf.productId)
        .distinct()
        .count();
  }

  @Override
  public boolean exists(String warehouseBusinessUnitCode, Long productId, Long storeId) {
    return count("warehouseBusinessUnitCode = ?1 and productId = ?2 and storeId = ?3",
        warehouseBusinessUnitCode, productId, storeId) > 0;
  }

  @Override
  public boolean warehouseAlreadyServesStore(String warehouseCode, Long storeId) {
    return find("warehouseBusinessUnitCode = ?1 and storeId = ?2", warehouseCode, storeId)
        .count() > 0;
  }

  @Override
  public boolean warehouseAlreadyStoresProduct(String warehouseCode, Long productId) {
    return find("warehouseBusinessUnitCode = ?1 and productId = ?2", warehouseCode, productId)
        .count() > 0;
  }

  public List<WarehouseFulfillment> findByWarehouse(String warehouseBusinessUnitCode) {
    return list("warehouseBusinessUnitCode = ?1", warehouseBusinessUnitCode);
  }
}
