package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StockWithinCapacityValidatorTest {

  // No mocks needed — this validator has no dependencies
  private final StockWithinCapacityValidator validator = new StockWithinCapacityValidator();

  private Warehouse warehouse;

  @BeforeEach
  void setUp() {
    warehouse = new Warehouse();
  }

  // ─── Happy path: stock equals capacity ────────────────────────────────────

  @Test
  void shouldPass_WhenStockEqualsCapacity() {
    warehouse.capacity = 100;
    warehouse.stock = 100;

    assertDoesNotThrow(() -> validator.validate(warehouse));
  }

  // ─── Happy path: stock below capacity ─────────────────────────────────────

  @Test
  void shouldPass_WhenStockIsBelowCapacity() {
    warehouse.capacity = 100;
    warehouse.stock = 50;

    assertDoesNotThrow(() -> validator.validate(warehouse));
  }

  // ─── Happy path: stock is null ────────────────────────────────────────────

  @Test
  void shouldPass_WhenStockIsNull() {
    warehouse.capacity = 100;
    warehouse.stock = null;

    assertDoesNotThrow(() -> validator.validate(warehouse));
  }

  // ─── Happy path: capacity is null ─────────────────────────────────────────

  @Test
  void shouldPass_WhenCapacityIsNull() {
    warehouse.capacity = null;
    warehouse.stock = 50;

    assertDoesNotThrow(() -> validator.validate(warehouse));
  }

  // ─── Failure: stock exceeds capacity by 1 ─────────────────────────────────

  @Test
  void shouldThrowBadRequest_WhenStockExceedsCapacityByOne() {
    warehouse.capacity = 100;
    warehouse.stock = 101;

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> validator.validate(warehouse));
    assertTrue(ex.getMessage().contains("101"));
    assertTrue(ex.getMessage().contains("100"));
  }

  // ─── Failure: stock greatly exceeds capacity ──────────────────────────────

  @Test
  void shouldThrowBadRequest_WhenStockFarExceedsCapacity() {
    warehouse.capacity = 50;
    warehouse.stock = 500;

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> validator.validate(warehouse));
    assertTrue(ex.getMessage().contains("500"));
    assertTrue(ex.getMessage().contains("50"));
  }
}
