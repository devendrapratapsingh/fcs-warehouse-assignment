package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

/**
 * Common interface for all warehouse validators.
 * Each validator checks ONE rule only — Single Responsibility.
 */
public interface WarehouseValidator {
  void validate(Warehouse warehouse);
}
