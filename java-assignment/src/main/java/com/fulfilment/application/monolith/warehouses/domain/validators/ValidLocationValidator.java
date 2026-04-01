package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

/**
 * Validates that the location specified in the warehouse actually exists.
 */
@ApplicationScoped
public class ValidLocationValidator implements WarehouseValidator {

  private final LocationResolver locationResolver;

  public ValidLocationValidator(LocationResolver locationResolver) {
    this.locationResolver = locationResolver;
  }

  @Override
  public void validate(Warehouse warehouse) {
    try {
      Location location = locationResolver.resolveByIdentifier(warehouse.location);
      if (location == null) {
        throw new BadRequestException("Location '" + warehouse.location + "' is not valid.");
      }
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Location '" + warehouse.location + "' is not valid.");
    }
  }
}
