package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validators.LocationCapacityValidator;
import com.fulfilment.application.monolith.warehouses.domain.validators.MaxWarehousesPerLocationValidator;
import com.fulfilment.application.monolith.warehouses.domain.validators.StockWithinCapacityValidator;
import com.fulfilment.application.monolith.warehouses.domain.validators.UniqueBusinessUnitCodeValidator;
import com.fulfilment.application.monolith.warehouses.domain.validators.ValidLocationValidator;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWarehouseUseCaseTest {

  @Mock WarehouseStore warehouseStore;
  @Mock UniqueBusinessUnitCodeValidator uniqueCodeValidator;
  @Mock ValidLocationValidator validLocationValidator;
  @Mock MaxWarehousesPerLocationValidator maxWarehousesValidator;
  @Mock LocationCapacityValidator locationCapacityValidator;
  @Mock StockWithinCapacityValidator stockWithinCapacityValidator;

  @InjectMocks CreateWarehouseUseCase useCase;

  private Warehouse warehouse;

  @BeforeEach
  void setUp() {
    warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "AMSTERDAM";
    warehouse.capacity = 100;
    warehouse.stock = 50;
  }

  // ─── Happy path ────────────────────────────────────────────────────────────

  @Test
  void shouldCreateWarehouse_WhenAllValidationsPass() {
    // all mocked validators do nothing by default
    assertDoesNotThrow(() -> useCase.create(warehouse));

    verify(uniqueCodeValidator).validate(warehouse);
    verify(validLocationValidator).validate(warehouse);
    verify(maxWarehousesValidator).validate(warehouse);
    verify(locationCapacityValidator).validate(warehouse);
    verify(stockWithinCapacityValidator).validate(warehouse);
    verify(warehouseStore).create(warehouse);
  }

  // ─── Failure: duplicate business unit code ────────────────────────────────

  @Test
  void shouldThrowBadRequest_WhenBusinessUnitCodeAlreadyExists() {
    doThrow(new BadRequestException("Business unit code 'MWH.001' already exists."))
        .when(uniqueCodeValidator).validate(warehouse);

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> useCase.create(warehouse));
    assertTrue(ex.getMessage().contains("MWH.001"));
    verify(warehouseStore, never()).create(any());
  }

  // ─── Failure: invalid location ────────────────────────────────────────────

  @Test
  void shouldThrowBadRequest_WhenLocationIsInvalid() {
    doThrow(new BadRequestException("Location 'UNKNOWN' is not valid."))
        .when(validLocationValidator).validate(warehouse);

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> useCase.create(warehouse));
    assertTrue(ex.getMessage().contains("not valid"));
    verify(warehouseStore, never()).create(any());
  }

  // ─── Failure: max warehouses per location reached ─────────────────────────

  @Test
  void shouldThrowBadRequest_WhenLocationHasReachedMaxWarehouses() {
    doThrow(new BadRequestException("Maximum number of warehouses already reached for location 'AMSTERDAM'."))
        .when(maxWarehousesValidator).validate(warehouse);

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> useCase.create(warehouse));
    assertTrue(ex.getMessage().contains("Maximum number of warehouses"));
    verify(warehouseStore, never()).create(any());
  }

  // ─── Failure: location capacity would be exceeded ─────────────────────────

  @Test
  void shouldThrowBadRequest_WhenLocationCapacityWouldBeExceeded() {
    doThrow(new BadRequestException("Total capacity (600) would exceed the maximum allowed (500)."))
        .when(locationCapacityValidator).validate(warehouse);

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> useCase.create(warehouse));
    assertTrue(ex.getMessage().contains("exceed"));
    verify(warehouseStore, never()).create(any());
  }

  // ─── Failure: stock exceeds own capacity ──────────────────────────────────

  @Test
  void shouldThrowBadRequest_WhenStockExceedsWarehouseCapacity() {
    doThrow(new BadRequestException("Stock (150) cannot exceed warehouse capacity (100)."))
        .when(stockWithinCapacityValidator).validate(warehouse);

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> useCase.create(warehouse));
    assertTrue(ex.getMessage().contains("exceed warehouse capacity"));
    verify(warehouseStore, never()).create(any());
  }
}
