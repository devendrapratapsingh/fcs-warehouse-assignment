package com.fulfilment.application.monolith.fulfillment.domain.ports;

import com.fulfilment.application.monolith.fulfillment.domain.models.Fulfillment;

/**
 * Port (driver): inbound operation to associate a Warehouse with a Product for a Store.
 */
public interface AssociateFulfillmentOperation {

  Fulfillment associate(String warehouseBusinessUnitCode, Long productId, Long storeId);
}
