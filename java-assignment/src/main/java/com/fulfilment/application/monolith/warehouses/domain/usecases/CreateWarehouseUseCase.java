package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validators.LocationCapacityValidator;
import com.fulfilment.application.monolith.warehouses.domain.validators.MaxWarehousesPerLocationValidator;
import com.fulfilment.application.monolith.warehouses.domain.validators.StockWithinCapacityValidator;
import com.fulfilment.application.monolith.warehouses.domain.validators.UniqueBusinessUnitCodeValidator;
import com.fulfilment.application.monolith.warehouses.domain.validators.ValidLocationValidator;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final UniqueBusinessUnitCodeValidator uniqueCodeValidator;
  private final ValidLocationValidator validLocationValidator;
  private final MaxWarehousesPerLocationValidator maxWarehousesValidator;
  private final LocationCapacityValidator locationCapacityValidator;
  private final StockWithinCapacityValidator stockWithinCapacityValidator;

  public CreateWarehouseUseCase(
      WarehouseStore warehouseStore,
      UniqueBusinessUnitCodeValidator uniqueCodeValidator,
      ValidLocationValidator validLocationValidator,
      MaxWarehousesPerLocationValidator maxWarehousesValidator,
      LocationCapacityValidator locationCapacityValidator,
      StockWithinCapacityValidator stockWithinCapacityValidator) {
    this.warehouseStore = warehouseStore;
    this.uniqueCodeValidator = uniqueCodeValidator;
    this.validLocationValidator = validLocationValidator;
    this.maxWarehousesValidator = maxWarehousesValidator;
    this.locationCapacityValidator = locationCapacityValidator;
    this.stockWithinCapacityValidator = stockWithinCapacityValidator;
  }

  @Override
  public void create(Warehouse warehouse) {
    LOGGER.infof("Creating warehouse with business unit code '%s' at location '%s'",
        warehouse.businessUnitCode, warehouse.location);
    uniqueCodeValidator.validate(warehouse);
    validLocationValidator.validate(warehouse);
    maxWarehousesValidator.validate(warehouse);
    locationCapacityValidator.validate(warehouse);
    stockWithinCapacityValidator.validate(warehouse);

    warehouseStore.create(warehouse);
    LOGGER.infof("Warehouse '%s' created successfully", warehouse.businessUnitCode);
  }
}
