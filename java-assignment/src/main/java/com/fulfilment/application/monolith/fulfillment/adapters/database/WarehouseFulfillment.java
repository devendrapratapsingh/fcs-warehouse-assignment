package com.fulfilment.application.monolith.fulfillment.adapters.database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity: Associates a Warehouse with a Product for a specific Store.
 *
 * Rules enforced via AssociateFulfillmentUseCase:
 *  - Max 2 warehouses per product per store
 *  - Max 3 warehouses per store
 *  - Max 5 product types per warehouse
 */
@Entity
@Table(name = "warehouse_fulfillment")
public class WarehouseFulfillment {

  @Id @GeneratedValue
  public Long id;

  public String warehouseBusinessUnitCode;
  public Long productId;
  public Long storeId;

  public WarehouseFulfillment() {}

  public WarehouseFulfillment(String warehouseBusinessUnitCode, Long productId, Long storeId) {
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    this.productId = productId;
    this.storeId = storeId;
  }
}
