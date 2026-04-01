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
class LocationCapacityValidatorTest {

  @Mock WarehouseStore warehouseStore;
  @Mock LocationResolver locationResolver;
  @InjectMocks LocationCapacityValidator validator;

  // Location max capacity = 500
  private final Location location = new Location("AMSTERDAM", 3, 500);

  @BeforeEach
  void setUp() {
    when(locationResolver.resolveByIdentifier("AMSTERDAM")).thenReturn(location);
  }

  private Warehouse warehouseWithCapacity(int capacity) {
    Warehouse w = new Warehouse();
    w.location = "AMSTERDAM";
    w.capacity = capacity;
    return w;
  }

  // ─── Happy path: no existing warehouses ───────────────────────────────────

  @Test
  void shouldPass_WhenLocationHasNoExistingCapacity() {
    when(warehouseStore.findActiveByLocation("AMSTERDAM")).thenReturn(Collections.emptyList());

    assertDoesNotThrow(() -> validator.validate(warehouseWithCapacity(500)));
  }

  // ─── Happy path: total capacity exactly at limit ──────────────────────────

  @Test
  void shouldPass_WhenTotalCapacityIsExactlyAtMaximum() {
    Warehouse existing = warehouseWithCapacity(300);
    when(warehouseStore.findActiveByLocation("AMSTERDAM")).thenReturn(List.of(existing));

    // 300 existing + 200 new = 500 (exactly at limit)
    assertDoesNotThrow(() -> validator.validate(warehouseWithCapacity(200)));
  }

  // ─── Happy path: total capacity below limit ───────────────────────────────

  @Test
  void shouldPass_WhenTotalCapacityIsBelowMaximum() {
    Warehouse existing = warehouseWithCapacity(100);
    when(warehouseStore.findActiveByLocation("AMSTERDAM")).thenReturn(List.of(existing));

    assertDoesNotThrow(() -> validator.validate(warehouseWithCapacity(200)));
  }

  // ─── Failure: total capacity exceeds limit by 1 ───────────────────────────

  @Test
  void shouldThrowBadRequest_WhenTotalCapacityExceedsMaximumByOne() {
    Warehouse existing = warehouseWithCapacity(400);
    when(warehouseStore.findActiveByLocation("AMSTERDAM")).thenReturn(List.of(existing));

    // 400 + 101 = 501, exceeds 500
    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> validator.validate(warehouseWithCapacity(101)));
    assertTrue(ex.getMessage().contains("501"));
    assertTrue(ex.getMessage().contains("500"));
    assertTrue(ex.getMessage().contains("AMSTERDAM"));
  }

  // ─── Failure: total capacity greatly exceeds limit ────────────────────────

  @Test
  void shouldThrowBadRequest_WhenTotalCapacityFarExceedsMaximum() {
    Warehouse e1 = warehouseWithCapacity(200);
    Warehouse e2 = warehouseWithCapacity(200);
    when(warehouseStore.findActiveByLocation("AMSTERDAM")).thenReturn(List.of(e1, e2));

    // 200 + 200 + 300 = 700, exceeds 500
    assertThrows(BadRequestException.class,
        () -> validator.validate(warehouseWithCapacity(300)));
  }
}
