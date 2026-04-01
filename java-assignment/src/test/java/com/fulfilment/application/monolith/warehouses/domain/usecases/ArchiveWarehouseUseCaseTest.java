package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
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
class ArchiveWarehouseUseCaseTest {

  @Mock WarehouseStore warehouseStore;
  @InjectMocks ArchiveWarehouseUseCase useCase;

  private Warehouse existing;
  private Warehouse request;

  @BeforeEach
  void setUp() {
    existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.capacity = 100;
    existing.stock = 50;
    existing.archivedAt = null;

    request = new Warehouse();
    request.businessUnitCode = "MWH.001";
  }

  // ─── Happy path ────────────────────────────────────────────────────────────

  @Test
  void shouldArchiveWarehouse_WhenWarehouseExists() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    assertDoesNotThrow(() -> useCase.archive(request));

    ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseStore).update(captor.capture());
    assertNotNull(captor.getValue().archivedAt,
        "archivedAt must be set after archiving");
  }

  // ─── Failure: warehouse not found ─────────────────────────────────────────

  @Test
  void shouldThrowNotFound_WhenWarehouseDoesNotExist() {
    when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(null);

    request.businessUnitCode = "MWH.999";
    NotFoundException ex = assertThrows(NotFoundException.class,
        () -> useCase.archive(request));
    assertTrue(ex.getMessage().contains("MWH.999"));
    verify(warehouseStore, never()).update(any());
  }

  // ─── Verify archivedAt is set to a recent timestamp ───────────────────────

  @Test
  void shouldSetArchivedAtToCurrentTime_WhenArchiving() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    long before = System.currentTimeMillis();
    useCase.archive(request);
    long after = System.currentTimeMillis();

    ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseStore).update(captor.capture());

    long archivedMillis = captor.getValue().archivedAt
        .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    assertTrue(archivedMillis >= before && archivedMillis <= after,
        "archivedAt must be set to the current time");
  }
}
