package com.fulfilment.application.monolith.fulfillment.domain.models;

/**
 * Domain model representing a fulfillment association between a Warehouse, a Product and a Store.
 */
public class Fulfillment {

  public String warehouseBusinessUnitCode;
  public Long productId;
  public Long storeId;

  public Fulfillment() {}

  public Fulfillment(String warehouseBusinessUnitCode, Long productId, Long storeId) {
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    this.productId = productId;
    this.storeId = storeId;
  }
}
