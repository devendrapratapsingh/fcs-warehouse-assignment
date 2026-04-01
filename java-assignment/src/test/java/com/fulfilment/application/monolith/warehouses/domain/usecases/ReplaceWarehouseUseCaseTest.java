package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validators.StockWithinCapacityValidator;
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

  @InjectMocks ReplaceWarehouseUseCase useCase;

  private Warehouse existing;
  private Warehouse replacement;

  @BeforeEach
  void setUp() {
    existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.capacity = 100;
    existing.stock = 50;
    existing.archivedAt = null;

    replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.capacity = 200;
    replacement.stock = 50; // must match existing stock
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
