package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.domain.models.Fulfillment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.AssociateFulfillmentOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.fulfillment.adapters.database.FulfillmentRepository;
import com.fulfilment.application.monolith.fulfillment.adapters.database.WarehouseFulfillment;
import com.fulfilment.application.monolith.fulfillment.domain.validators.MaxProductTypesPerWarehouseValidator;
import com.fulfilment.application.monolith.fulfillment.domain.validators.MaxWarehousesPerProductPerStoreValidator;
import com.fulfilment.application.monolith.fulfillment.domain.validators.MaxWarehousesPerStoreValidator;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.StoreRepository;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

/**
 * Use case: Associate a Warehouse with a Product for a Store.
 * Domain logic lives here; persistence is delegated via ports/adapters.
 */
@ApplicationScoped
public class AssociateFulfillmentUseCase implements AssociateFulfillmentOperation {

  private static final Logger LOGGER = Logger.getLogger(AssociateFulfillmentUseCase.class);

  private final FulfillmentStore fulfillmentStore;
  private final FulfillmentRepository fulfillmentRepository;
  private final WarehouseRepository warehouseRepository;
  private final ProductRepository productRepository;
  private final StoreRepository storeRepository;
  private final MaxWarehousesPerProductPerStoreValidator maxWarehousesPerProductPerStoreValidator;
  private final MaxWarehousesPerStoreValidator maxWarehousesPerStoreValidator;
  private final MaxProductTypesPerWarehouseValidator maxProductTypesPerWarehouseValidator;

  public AssociateFulfillmentUseCase(
      FulfillmentStore fulfillmentStore,
      FulfillmentRepository fulfillmentRepository,
      WarehouseRepository warehouseRepository,
      ProductRepository productRepository,
      StoreRepository storeRepository,
      MaxWarehousesPerProductPerStoreValidator maxWarehousesPerProductPerStoreValidator,
      MaxWarehousesPerStoreValidator maxWarehousesPerStoreValidator,
      MaxProductTypesPerWarehouseValidator maxProductTypesPerWarehouseValidator) {
    this.fulfillmentStore = fulfillmentStore;
    this.fulfillmentRepository = fulfillmentRepository;
    this.warehouseRepository = warehouseRepository;
    this.productRepository = productRepository;
    this.storeRepository = storeRepository;
    this.maxWarehousesPerProductPerStoreValidator = maxWarehousesPerProductPerStoreValidator;
    this.maxWarehousesPerStoreValidator = maxWarehousesPerStoreValidator;
    this.maxProductTypesPerWarehouseValidator = maxProductTypesPerWarehouseValidator;
  }

  @Override
  public Fulfillment associate(String warehouseBusinessUnitCode, Long productId, Long storeId) {
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

    // Business rule validations
    maxWarehousesPerProductPerStoreValidator.validate(warehouseBusinessUnitCode, productId, storeId);
    maxWarehousesPerStoreValidator.validate(warehouseBusinessUnitCode, storeId);
    maxProductTypesPerWarehouseValidator.validate(warehouseBusinessUnitCode, productId);

    // Persist via adapter
    var entity = new WarehouseFulfillment(warehouseBusinessUnitCode, productId, storeId);
    fulfillmentRepository.persist(entity);
    LOGGER.infof("Fulfillment association created: warehouse=%s, product=%d, store=%d",
        warehouseBusinessUnitCode, productId, storeId);

    return new Fulfillment(warehouseBusinessUnitCode, productId, storeId);
  }
}
