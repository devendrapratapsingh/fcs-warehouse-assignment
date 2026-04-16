package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.adapters.database.FulfillmentRepository;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.fulfillment.domain.validators.MaxProductTypesPerWarehouseValidator;
import com.fulfilment.application.monolith.fulfillment.domain.validators.MaxWarehousesPerProductPerStoreValidator;
import com.fulfilment.application.monolith.fulfillment.domain.validators.MaxWarehousesPerStoreValidator;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.stores.StoreRepository;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssociateFulfillmentUseCaseTest {

  @Mock FulfillmentRepository fulfillmentRepository;
  @Mock FulfillmentStore fulfillmentStore;
  @Mock WarehouseRepository warehouseRepository;
  @Mock ProductRepository productRepository;
  @Mock StoreRepository storeRepository;
  @Mock MaxWarehousesPerProductPerStoreValidator maxWarehousesPerProductPerStoreValidator;
  @Mock MaxWarehousesPerStoreValidator maxWarehousesPerStoreValidator;
  @Mock MaxProductTypesPerWarehouseValidator maxProductTypesPerWarehouseValidator;

  private AssociateFulfillmentUseCase useCase;

  private Warehouse mockWarehouse;
  private Product mockProduct;
  private Store mockStore;

  @BeforeEach
  void setUp() {
    useCase = new AssociateFulfillmentUseCase(
        fulfillmentStore,
        fulfillmentRepository,
        warehouseRepository,
        productRepository,
        storeRepository,
        maxWarehousesPerProductPerStoreValidator,
        maxWarehousesPerStoreValidator,
        maxProductTypesPerWarehouseValidator);

    mockWarehouse = new Warehouse();
    mockWarehouse.businessUnitCode = "MWH.001";
    mockProduct = new Product("KALLAX");
    mockProduct.id = 1L;
    mockStore = new Store("AMSTERDAM-STORE");
    mockStore.id = 1L;
  }

  // ─── Happy Path ────────────────────────────────────────────────────────────
  @Test
  void shouldAssociateSuccessfullyWhenAllRulesPass() {
    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(mockWarehouse);
    when(productRepository.findById(1L)).thenReturn(mockProduct);
    when(storeRepository.findById(1L)).thenReturn(mockStore);
    when(fulfillmentStore.exists("MWH.001", 1L, 1L)).thenReturn(false);

    assertDoesNotThrow((org.junit.jupiter.api.function.Executable)
        () -> useCase.associate("MWH.001", 1L, 1L));
  }

  // ─── Rule: Warehouse must exist ────────────────────────────────────────────
  @Test
  void shouldThrowNotFoundWhenWarehouseDoesNotExist() {
    when(warehouseRepository.findByBusinessUnitCode("INVALID")).thenReturn(null);

    NotFoundException ex = assertThrows(NotFoundException.class,
        (org.junit.jupiter.api.function.Executable)
        () -> useCase.associate("INVALID", 1L, 1L));
    assertTrue(ex.getMessage().contains("INVALID"));
  }

  // ─── Rule: Product must exist ──────────────────────────────────────────────
  @Test
  void shouldThrowNotFoundWhenProductDoesNotExist() {
    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(mockWarehouse);
    when(productRepository.findById(99L)).thenReturn(null);

    NotFoundException ex = assertThrows(NotFoundException.class,
        (org.junit.jupiter.api.function.Executable)
        () -> useCase.associate("MWH.001", 99L, 1L));
    assertTrue(ex.getMessage().contains("99"));
  }

  // ─── Rule: Duplicate association not allowed ───────────────────────────────
  @Test
  void shouldThrowBadRequestWhenAssociationAlreadyExists() {
    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(mockWarehouse);
    when(productRepository.findById(1L)).thenReturn(mockProduct);
    when(storeRepository.findById(1L)).thenReturn(mockStore);
    when(fulfillmentStore.exists("MWH.001", 1L, 1L)).thenReturn(true);

    BadRequestException ex = assertThrows(BadRequestException.class,
        (org.junit.jupiter.api.function.Executable)
        () -> useCase.associate("MWH.001", 1L, 1L));
    assertTrue(ex.getMessage().contains("already exists"));
  }

  // ─── Rule 1: Max 2 warehouses per product per store ───────────────────────
  @Test
  void shouldThrowBadRequestWhenProductAlreadyHas2WarehousesForStore() {
    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(mockWarehouse);
    when(productRepository.findById(1L)).thenReturn(mockProduct);
    when(storeRepository.findById(1L)).thenReturn(mockStore);
    when(fulfillmentStore.exists("MWH.001", 1L, 1L)).thenReturn(false);
    doThrow(new BadRequestException("Product 1 already has 2 warehouses assigned for store 1."))
        .when(maxWarehousesPerProductPerStoreValidator).validate("MWH.001", 1L, 1L);

    BadRequestException ex = assertThrows(BadRequestException.class,
        (org.junit.jupiter.api.function.Executable)
        () -> useCase.associate("MWH.001", 1L, 1L));
    assertTrue(ex.getMessage().contains("2 warehouses"));
  }

  // ─── Rule 2: Max 3 warehouses per store ───────────────────────────────────
  @Test
  void shouldThrowBadRequestWhenStoreAlreadyHas3Warehouses() {
    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(mockWarehouse);
    when(productRepository.findById(1L)).thenReturn(mockProduct);
    when(storeRepository.findById(1L)).thenReturn(mockStore);
    when(fulfillmentStore.exists("MWH.001", 1L, 1L)).thenReturn(false);
    doThrow(new BadRequestException("Store 1 already has 3 warehouses assigned."))
        .when(maxWarehousesPerStoreValidator).validate("MWH.001", 1L);

    BadRequestException ex = assertThrows(BadRequestException.class,
        (org.junit.jupiter.api.function.Executable)
        () -> useCase.associate("MWH.001", 1L, 1L));
    assertTrue(ex.getMessage().contains("3 warehouses"));
  }

  // ─── Rule 3: Max 5 product types per warehouse ────────────────────────────
  @Test
  void shouldThrowBadRequestWhenWarehouseAlreadyHas5ProductTypes() {
    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(mockWarehouse);
    when(productRepository.findById(1L)).thenReturn(mockProduct);
    when(storeRepository.findById(1L)).thenReturn(mockStore);
    when(fulfillmentStore.exists("MWH.001", 1L, 1L)).thenReturn(false);
    doThrow(new BadRequestException("Warehouse 'MWH.001' already stores 5 product types."))
        .when(maxProductTypesPerWarehouseValidator).validate("MWH.001", 1L);

    BadRequestException ex = assertThrows(BadRequestException.class,
        (org.junit.jupiter.api.function.Executable)
        () -> useCase.associate("MWH.001", 1L, 1L));
    assertTrue(ex.getMessage().contains("5 product types"));
  }
}
