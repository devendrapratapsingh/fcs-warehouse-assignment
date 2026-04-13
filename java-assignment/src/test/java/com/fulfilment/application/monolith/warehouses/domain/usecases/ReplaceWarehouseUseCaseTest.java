package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validators.LocationCapacityValidator;
import com.fulfilment.application.monolith.warehouses.domain.validators.MaxWarehousesPerLocationValidator;
import com.fulfilment.application.monolith.warehouses.domain.validators.StockWithinCapacityValidator;
import com.fulfilment.application.monolith.warehouses.domain.validators.ValidLocationValidator;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplaceWarehouseUseCaseTest {

  @Mock WarehouseStore warehouseStore;
  @Mock StockWithinCapacityValidator stockWithinCapacityValidator;
  @Mock ValidLocationValidator validLocationValidator;
  @Mock MaxWarehousesPerLocationValidator maxWarehousesPerLocationValidator;
  @Mock LocationCapacityValidator locationCapacityValidator;

  @InjectMocks ReplaceWarehouseUseCase useCase;

  private Warehouse existing;
  private Warehouse replacement;

  @BeforeEach
  void setUp() {
    existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.capacity = 100;
    existing.stock = 50;
    existing.location = "AMSTERDAM-001";
    existing.archivedAt = null;

    replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.capacity = 200;
    replacement.stock = 50; // must match existing stock
    replacement.location = "AMSTERDAM-001";
  }

  // ─── Happy path ────────────────────────────────────────────────────────────

  @Test
  void shouldReplaceWarehouse_WhenAllConditionsPass() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    assertDoesNotThrow(() -> useCase.replace(replacement));

    // Old warehouse archived
    ArgumentCaptor<Warehouse> updateCaptor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseStore).update(updateCaptor.capture());
    assertNotNull(updateCaptor.getValue().archivedAt, "old warehouse must be archived");

    // New warehouse created
    verify(warehouseStore).create(replacement);
    verify(stockWithinCapacityValidator).validate(replacement);
    verify(validLocationValidator).validate(replacement);
    verify(maxWarehousesPerLocationValidator).validate(replacement);
    verify(locationCapacityValidator).validate(replacement);
  }

  // ─── Failure: warehouse not found ─────────────────────────────────────────

  @Test
  void shouldThrowNotFound_WhenWarehouseDoesNotExist() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null);

    NotFoundException ex = assertThrows(NotFoundException.class,
        () -> useCase.replace(replacement));
    assertTrue(ex.getMessage().contains("MWH.001"));
    verify(warehouseStore, never()).create(any());
    verify(warehouseStore, never()).update(any());
  }

  // ─── Failure: stock mismatch ──────────────────────────────────────────────

  @Test
  void shouldThrowBadRequest_WhenNewStockDoesNotMatchExistingStock() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    replacement.stock = 99; // differs from existing.stock = 50

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> useCase.replace(replacement));
    assertTrue(ex.getMessage().contains("99"));
    assertTrue(ex.getMessage().contains("50"));
    verify(warehouseStore, never()).create(any());
  }

  // ─── Failure: new capacity too small for stock ────────────────────────────

  @Test
  void shouldThrowBadRequest_WhenNewCapacityCannotAccommodateStock() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    doThrow(new BadRequestException("Stock (50) cannot exceed warehouse capacity (30)."))
        .when(stockWithinCapacityValidator).validate(replacement);

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> useCase.replace(replacement));
    assertTrue(ex.getMessage().contains("exceed warehouse capacity"));
    verify(warehouseStore, never()).create(any());
  }

  // ─── Failure: invalid location ────────────────────────────────────────────

  @Test
  void shouldThrowNotFound_WhenNewLocationIsInvalid() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    replacement.location = "NOWHERE-999";
    doThrow(new NotFoundException("Location with identifier NOWHERE-999 not found"))
        .when(validLocationValidator).validate(replacement);

    NotFoundException ex = assertThrows(NotFoundException.class,
        () -> useCase.replace(replacement));
    assertTrue(ex.getMessage().contains("NOWHERE-999"));
    verify(warehouseStore, never()).create(any());
    // Old warehouse should have been archived before location validation
    verify(warehouseStore).update(existing);
  }

  // ─── Failure: max warehouses per location exceeded ────────────────────────

  @Test
  void shouldThrowBadRequest_WhenMaxWarehousesPerLocationExceeded() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    doThrow(new BadRequestException("Maximum number of warehouses already reached for location 'AMSTERDAM-001'."))
        .when(maxWarehousesPerLocationValidator).validate(replacement);

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> useCase.replace(replacement));
    assertTrue(ex.getMessage().contains("Maximum number of warehouses"));
    verify(warehouseStore, never()).create(any());
  }

  // ─── Failure: location capacity exceeded ──────────────────────────────────

  @Test
  void shouldThrowBadRequest_WhenLocationCapacityExceeded() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    doThrow(new BadRequestException("Total capacity (600) would exceed the maximum allowed (500)."))
        .when(locationCapacityValidator).validate(replacement);

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> useCase.replace(replacement));
    assertTrue(ex.getMessage().contains("exceed"));
    verify(warehouseStore, never()).create(any());
  }

  // ─── Verify old is archived before new is created ─────────────────────────

  @Test
  void shouldArchiveOldBeforeCreatingNew_WhenReplacing() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    useCase.replace(replacement);

    // Verify order: update (archive) happens before create
    var inOrder = inOrder(warehouseStore);
    inOrder.verify(warehouseStore).update(existing);
    inOrder.verify(warehouseStore).create(replacement);
  }
}
