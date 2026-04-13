package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.StoreRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.warehouses.domain.validators.MaxProductTypesPerWarehouseValidator;
import com.fulfilment.application.monolith.warehouses.domain.validators.MaxWarehousesPerProductPerStoreValidator;
import com.fulfilment.application.monolith.warehouses.domain.validators.MaxWarehousesPerStoreValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

@ApplicationScoped
public class FulfillmentService {

  private static final Logger LOGGER = Logger.getLogger(FulfillmentService.class);

  @Inject FulfillmentRepository fulfillmentRepository;
  @Inject FulfillmentStore fulfillmentStore;
  @Inject WarehouseRepository warehouseRepository;
  @Inject ProductRepository productRepository;
  @Inject StoreRepository storeRepository;

  @Inject MaxWarehousesPerProductPerStoreValidator maxWarehousesPerProductPerStoreValidator;
  @Inject MaxWarehousesPerStoreValidator maxWarehousesPerStoreValidator;
  @Inject MaxProductTypesPerWarehouseValidator maxProductTypesPerWarehouseValidator;

  @Transactional
  public WarehouseFulfillment associate(
      String warehouseBusinessUnitCode, Long productId, Long storeId) {

    LOGGER.infof("Associating warehouse '%s' with product %d for store %d",
        warehouseBusinessUnitCode, productId, storeId);

    // Existence checks
    if (warehouseRepository.findByBusinessUnitCode(warehouseBusinessUnitCode) == null) {
      LOGGER.warnf("Associate failed — warehouse '%s' not found", warehouseBusinessUnitCode);
      throw new NotFoundException("Warehouse '" + warehouseBusinessUnitCode + "' not found.");
    }
    if (productRepository.findById(productId) == null) {
      LOGGER.warnf("Associate failed — product %d not found", productId);
      throw new NotFoundException("Product with id " + productId + " not found.");
    }
    if (storeRepository.findById(storeId) == null) {
      LOGGER.warnf("Associate failed — store %d not found", storeId);
      throw new NotFoundException("Store with id " + storeId + " not found.");
    }
    if (fulfillmentStore.exists(warehouseBusinessUnitCode, productId, storeId)) {
      LOGGER.warnf("Associate failed — association already exists: warehouse=%s, product=%d, store=%d",
          warehouseBusinessUnitCode, productId, storeId);
      throw new BadRequestException("This warehouse-product-store association already exists.");
    }

    // Business rule validations — each rule in its own class
    maxWarehousesPerProductPerStoreValidator.validate(warehouseBusinessUnitCode, productId, storeId);
    maxWarehousesPerStoreValidator.validate(warehouseBusinessUnitCode, storeId);
    maxProductTypesPerWarehouseValidator.validate(warehouseBusinessUnitCode, productId);

    // All checks passed — persist
    WarehouseFulfillment fulfillment =
        new WarehouseFulfillment(warehouseBusinessUnitCode, productId, storeId);
    fulfillmentRepository.persist(fulfillment);
    LOGGER.infof("Fulfillment association created: warehouse=%s, product=%d, store=%d",
        warehouseBusinessUnitCode, productId, storeId);
    return fulfillment;
  }
}
