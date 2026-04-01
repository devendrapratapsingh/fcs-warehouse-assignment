package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

/**
 * Validates that the warehouse stock does not exceed its own capacity.
 * Reusable in both Create and Replace operations.
 */
@ApplicationScoped
public class StockWithinCapacityValidator implements WarehouseValidator {

  @Override
  public void validate(Warehouse warehouse) {
    if (warehouse.stock != null && warehouse.capacity != null
        && warehouse.stock > warehouse.capacity) {
      throw new BadRequestException(
          "Stock (" + warehouse.stock + ") cannot exceed warehouse capacity ("
              + warehouse.capacity + ").");
    }
  }
}
