package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

/**
 * Validates that the location has not reached its maximum number of warehouses.
 */
@ApplicationScoped
public class MaxWarehousesPerLocationValidator implements WarehouseValidator {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public MaxWarehousesPerLocationValidator(
      WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void validate(Warehouse warehouse) {
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    int activeCount = warehouseStore.findActiveByLocation(warehouse.location).size();
    if (activeCount >= location.getMaxNumberOfWarehouses()) {
      throw new BadRequestException(
          "Maximum number of warehouses already reached for location '" + warehouse.location + "'.");
    }
  }
}
