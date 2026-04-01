package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validators.StockWithinCapacityValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ReplaceWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final StockWithinCapacityValidator stockWithinCapacityValidator;

  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore, StockWithinCapacityValidator stockWithinCapacityValidator) {
    this.warehouseStore = warehouseStore;
    this.stockWithinCapacityValidator = stockWithinCapacityValidator;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    LOGGER.infof("Replacing warehouse '%s'", newWarehouse.businessUnitCode);

    // 1. Find the current active warehouse
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      LOGGER.warnf("Replace failed — warehouse '%s' not found", newWarehouse.businessUnitCode);
      throw new NotFoundException(
          "Warehouse with business unit code '" + newWarehouse.businessUnitCode + "' not found.");
    }

    // 2. New warehouse stock must match old warehouse stock
    if (!existing.stock.equals(newWarehouse.stock)) {
      LOGGER.warnf("Replace failed — stock mismatch for '%s': existing=%d, new=%d",
          newWarehouse.businessUnitCode, existing.stock, newWarehouse.stock);
      throw new BadRequestException(
          "New warehouse stock (" + newWarehouse.stock
              + ") must match the existing warehouse stock (" + existing.stock + ").");
    }

    // 3. New warehouse capacity must accommodate the existing stock (reused validator)
    stockWithinCapacityValidator.validate(newWarehouse);

    // 4. Archive the old warehouse
    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);
    LOGGER.infof("Archived old warehouse '%s' at %s", existing.businessUnitCode, existing.archivedAt);

    // 5. Create the new warehouse with the same business unit code
    warehouseStore.create(newWarehouse);
    LOGGER.infof("New warehouse '%s' created as replacement", newWarehouse.businessUnitCode);
  }
}
