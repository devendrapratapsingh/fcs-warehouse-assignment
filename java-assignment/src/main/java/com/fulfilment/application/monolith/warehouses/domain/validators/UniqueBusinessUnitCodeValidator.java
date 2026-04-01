package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

/**
 * Validates that the Business Unit Code is not already used by an active warehouse.
 */
@ApplicationScoped
public class UniqueBusinessUnitCodeValidator implements WarehouseValidator {

  private final WarehouseStore warehouseStore;

  public UniqueBusinessUnitCodeValidator(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void validate(Warehouse warehouse) {
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new BadRequestException(
          "Business unit code '" + warehouse.businessUnitCode + "' already exists.");
    }
  }
}
