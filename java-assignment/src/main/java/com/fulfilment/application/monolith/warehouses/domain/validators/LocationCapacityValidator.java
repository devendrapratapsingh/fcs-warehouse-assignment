package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

/**
 * Validates that adding this warehouse's capacity won't exceed the location's max total capacity.
 */
@ApplicationScoped
public class LocationCapacityValidator implements WarehouseValidator {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public LocationCapacityValidator(
      WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void validate(Warehouse warehouse) {
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    int existingCapacity = warehouseStore.findActiveByLocation(warehouse.location).stream()
        .mapToInt(w -> w.capacity)
        .sum();
    int totalCapacity = existingCapacity + warehouse.capacity;
    if (totalCapacity > location.getMaxCapacity()) {
      throw new BadRequestException(
          "Total capacity (" + totalCapacity + ") would exceed the maximum allowed ("
              + location.getMaxCapacity() + ") for location '" + warehouse.location + "'.");
    }
  }
}
