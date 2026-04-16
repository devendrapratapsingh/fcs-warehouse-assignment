package com.fulfilment.application.monolith.fulfillment.domain.validators;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for Rule 2: each Store can be fulfilled by max 3 Warehouses.
 */
@ExtendWith(MockitoExtension.class)
class MaxWarehousesPerStoreValidatorTest {

  @Mock FulfillmentStore fulfillmentStore;
  @InjectMocks MaxWarehousesPerStoreValidator validator;

  private static final String WAREHOUSE = "MWH.NEW";
  private static final Long   STORE     = 1L;

  @Test
  void shouldPass_whenStoreHasNoWarehousesYet() {
    when(fulfillmentStore.warehouseAlreadyServesStore(WAREHOUSE, STORE)).thenReturn(false);
    when(fulfillmentStore.countDistinctWarehousesForStore(STORE)).thenReturn(0L);
    assertDoesNotThrow(() -> validator.validate(WAREHOUSE, STORE));
  }

  @Test
  void shouldPass_whenStoreHasTwoWarehousesAndThisIsNew() {
    when(fulfillmentStore.warehouseAlreadyServesStore(WAREHOUSE, STORE)).thenReturn(false);
    when(fulfillmentStore.countDistinctWarehousesForStore(STORE)).thenReturn(2L);
    assertDoesNotThrow(() -> validator.validate(WAREHOUSE, STORE));
  }

  @Test
  void shouldPass_whenWarehouseAlreadyServesStore_skipsCountCheck() {
    when(fulfillmentStore.warehouseAlreadyServesStore(WAREHOUSE, STORE)).thenReturn(true);
    assertDoesNotThrow(() -> validator.validate(WAREHOUSE, STORE));
    verify(fulfillmentStore, never()).countDistinctWarehousesForStore(any());
  }

  @Test
  void shouldThrow_whenStoreAlreadyHasThreeWarehouses() {
    when(fulfillmentStore.warehouseAlreadyServesStore(WAREHOUSE, STORE)).thenReturn(false);
    when(fulfillmentStore.countDistinctWarehousesForStore(STORE)).thenReturn(3L);
    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> validator.validate(WAREHOUSE, STORE));
    assertTrue(ex.getMessage().contains("3 warehouses"));
    assertTrue(ex.getMessage().contains("Store " + STORE));
  }

  @Test
  void shouldThrow_whenCountExceedsLimit() {
    when(fulfillmentStore.warehouseAlreadyServesStore(WAREHOUSE, STORE)).thenReturn(false);
    when(fulfillmentStore.countDistinctWarehousesForStore(STORE)).thenReturn(5L);
    assertThrows(BadRequestException.class, () -> validator.validate(WAREHOUSE, STORE));
  }
}
