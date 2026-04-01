package com.fulfilment.application.monolith.warehouses.domain.validators;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.adapters.database.FulfillmentRepository;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for Rule 1: each Product can be fulfilled by max 2 Warehouses per Store.
 */
@ExtendWith(MockitoExtension.class)
class MaxWarehousesPerProductPerStoreValidatorTest {

  @Mock FulfillmentRepository fulfillmentRepository;

  @InjectMocks MaxWarehousesPerProductPerStoreValidator validator;

  private static final String WAREHOUSE = "MWH.001";
  private static final Long   PRODUCT   = 1L;
  private static final Long   STORE     = 1L;

  // ── Happy path ─────────────────────────────────────────────────────────────

  @Test
  void shouldPass_whenNoWarehousesAssignedYet() {
    when(fulfillmentRepository.countWarehousesForProductInStore(PRODUCT, STORE)).thenReturn(0L);
    assertDoesNotThrow(() -> validator.validate(WAREHOUSE, PRODUCT, STORE));
  }

  @Test
  void shouldPass_whenOneWarehouseAlreadyAssigned() {
    when(fulfillmentRepository.countWarehousesForProductInStore(PRODUCT, STORE)).thenReturn(1L);
    assertDoesNotThrow(() -> validator.validate(WAREHOUSE, PRODUCT, STORE));
  }

  // ── Boundary ───────────────────────────────────────────────────────────────

  @Test
  void shouldPass_atExactlyOneBelowLimit() {
    // MAX = 2, so count=1 is still allowed
    when(fulfillmentRepository.countWarehousesForProductInStore(PRODUCT, STORE)).thenReturn(1L);
    assertDoesNotThrow(() -> validator.validate(WAREHOUSE, PRODUCT, STORE));
  }

  // ── Failure ────────────────────────────────────────────────────────────────

  @Test
  void shouldThrow_whenMaxWarehousesReached() {
    when(fulfillmentRepository.countWarehousesForProductInStore(PRODUCT, STORE)).thenReturn(2L);
    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> validator.validate(WAREHOUSE, PRODUCT, STORE));
    assertTrue(ex.getMessage().contains("2 warehouses"));
    assertTrue(ex.getMessage().contains("store " + STORE));
  }

  @Test
  void shouldThrow_whenCountExceedsLimit() {
    // Guard against data anomaly where count is already above the limit
    when(fulfillmentRepository.countWarehousesForProductInStore(PRODUCT, STORE)).thenReturn(3L);
    assertThrows(BadRequestException.class,
        () -> validator.validate(WAREHOUSE, PRODUCT, STORE));
  }
}
