package com.fulfilment.application.monolith.warehouses.domain.validators;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.ports.FulfillmentStore;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for Rule 3: each Warehouse can store max 5 types of Products.
 */
@ExtendWith(MockitoExtension.class)
class MaxProductTypesPerWarehouseValidatorTest {

  @Mock FulfillmentStore fulfillmentStore;

  @InjectMocks MaxProductTypesPerWarehouseValidator validator;

  private static final String WAREHOUSE = "MWH.001";
  private static final Long   PRODUCT   = 99L;

  // ── Happy path ─────────────────────────────────────────────────────────────

  @Test
  void shouldPass_whenWarehouseStoresNoProductsYet() {
    when(fulfillmentStore.warehouseAlreadyStoresProduct(WAREHOUSE, PRODUCT)).thenReturn(false);
    when(fulfillmentStore.countDistinctProductsInWarehouse(WAREHOUSE)).thenReturn(0L);
    assertDoesNotThrow(() -> validator.validate(WAREHOUSE, PRODUCT));
  }

  @Test
  void shouldPass_whenWarehouseStoresFourProductTypes() {
    when(fulfillmentStore.warehouseAlreadyStoresProduct(WAREHOUSE, PRODUCT)).thenReturn(false);
    when(fulfillmentStore.countDistinctProductsInWarehouse(WAREHOUSE)).thenReturn(4L);
    assertDoesNotThrow(() -> validator.validate(WAREHOUSE, PRODUCT));
  }

  @Test
  void shouldPass_whenProductAlreadyStoredInWarehouse_skipsCountCheck() {
    // product is already stored → idempotent, count is not checked
    when(fulfillmentStore.warehouseAlreadyStoresProduct(WAREHOUSE, PRODUCT)).thenReturn(true);
    assertDoesNotThrow(() -> validator.validate(WAREHOUSE, PRODUCT));
    verify(fulfillmentStore, never()).countDistinctProductsInWarehouse(any());
  }

  // ── Boundary ───────────────────────────────────────────────────────────────

  @Test
  void shouldPass_atExactlyFourProductTypes() {
    // MAX = 5, so count=4 is the last allowed new product
    when(fulfillmentStore.warehouseAlreadyStoresProduct(WAREHOUSE, PRODUCT)).thenReturn(false);
    when(fulfillmentStore.countDistinctProductsInWarehouse(WAREHOUSE)).thenReturn(4L);
    assertDoesNotThrow(() -> validator.validate(WAREHOUSE, PRODUCT));
  }

  // ── Failure ────────────────────────────────────────────────────────────────

  @Test
  void shouldThrow_whenWarehouseAlreadyHasFiveProductTypes() {
    when(fulfillmentStore.warehouseAlreadyStoresProduct(WAREHOUSE, PRODUCT)).thenReturn(false);
    when(fulfillmentStore.countDistinctProductsInWarehouse(WAREHOUSE)).thenReturn(5L);
    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> validator.validate(WAREHOUSE, PRODUCT));
    assertTrue(ex.getMessage().contains("5 product types"));
    assertTrue(ex.getMessage().contains(WAREHOUSE));
  }

  @Test
  void shouldThrow_whenCountExceedsLimit() {
    when(fulfillmentStore.warehouseAlreadyStoresProduct(WAREHOUSE, PRODUCT)).thenReturn(false);
    when(fulfillmentStore.countDistinctProductsInWarehouse(WAREHOUSE)).thenReturn(7L);
    assertThrows(BadRequestException.class, () -> validator.validate(WAREHOUSE, PRODUCT));
  }
}
