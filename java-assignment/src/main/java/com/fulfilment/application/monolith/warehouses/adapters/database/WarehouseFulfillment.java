package com.fulfilment.application.monolith.warehouses.adapters.database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Join table: Associates a Warehouse to a Product for a specific Store.
 *
 * Rules enforced via FulfillmentService:
 *  - Max 2 warehouses per product per store
 *  - Max 3 warehouses per store
 *  - Max 5 product types per warehouse
 */
@Entity
@Table(name = "warehouse_fulfillment")
public class WarehouseFulfillment {

  @Id @GeneratedValue
  public Long id;

  // Business unit code of the warehouse
  public String warehouseBusinessUnitCode;

  // ID of the product being fulfilled
  public Long productId;

  // ID of the store being fulfilled
  public Long storeId;

  public WarehouseFulfillment() {}

  public WarehouseFulfillment(String warehouseBusinessUnitCode, Long productId, Long storeId) {
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    this.productId = productId;
    this.storeId = storeId;
  }
}
