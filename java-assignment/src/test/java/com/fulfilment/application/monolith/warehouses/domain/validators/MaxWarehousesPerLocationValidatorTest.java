package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaxWarehousesPerLocationValidatorTest {

  @Mock WarehouseStore warehouseStore;
  @Mock LocationResolver locationResolver;
  @InjectMocks MaxWarehousesPerLocationValidator validator;

  private Warehouse warehouse;
  // Location allows max 3 warehouses
  private final Location location = new Location("AMSTERDAM", 3, 1000);

  @BeforeEach
  void setUp() {
    warehouse = new Warehouse();
    warehouse.location = "AMSTERDAM";
    when(locationResolver.resolveByIdentifier("AMSTERDAM")).thenReturn(location);
  }

  // ─── Happy path: zero existing warehouses ─────────────────────────────────

  @Test
  void shouldPass_WhenLocationHasNoWarehouses() {
    when(warehouseStore.findActiveByLocation("AMSTERDAM")).thenReturn(Collections.emptyList());

    assertDoesNotThrow(() -> validator.validate(warehouse));
  }

  // ─── Happy path: exactly one below the limit ──────────────────────────────

  @Test
  void shouldPass_WhenLocationHasFewerWarehousesThanMaximum() {
    Warehouse w1 = new Warehouse();
    Warehouse w2 = new Warehouse();
    when(warehouseStore.findActiveByLocation("AMSTERDAM")).thenReturn(List.of(w1, w2)); // 2 of 3

    assertDoesNotThrow(() -> validator.validate(warehouse));
  }

  // ─── Boundary: exactly at limit ───────────────────────────────────────────

  @Test
  void shouldThrowBadRequest_WhenLocationHasReachedMaximumWarehouses() {
    Warehouse w1 = new Warehouse();
    Warehouse w2 = new Warehouse();
    Warehouse w3 = new Warehouse();
    when(warehouseStore.findActiveByLocation("AMSTERDAM")).thenReturn(List.of(w1, w2, w3)); // 3 of 3

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> validator.validate(warehouse));
    assertTrue(ex.getMessage().contains("AMSTERDAM"));
    assertTrue(ex.getMessage().contains("Maximum number of warehouses"));
  }

  // ─── Boundary: one over the limit ─────────────────────────────────────────

  @Test
  void shouldThrowBadRequest_WhenLocationHasMoreWarehousesThanMaximum() {
    List<Warehouse> fourWarehouses = List.of(
        new Warehouse(), new Warehouse(), new Warehouse(), new Warehouse());
    when(warehouseStore.findActiveByLocation("AMSTERDAM")).thenReturn(fourWarehouses);

    assertThrows(BadRequestException.class, () -> validator.validate(warehouse));
  }
}
