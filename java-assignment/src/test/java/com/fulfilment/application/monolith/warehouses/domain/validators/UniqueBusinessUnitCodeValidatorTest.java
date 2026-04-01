package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
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
class UniqueBusinessUnitCodeValidatorTest {

  @Mock WarehouseStore warehouseStore;
  @InjectMocks UniqueBusinessUnitCodeValidator validator;

  private Warehouse warehouse;

  @BeforeEach
  void setUp() {
    warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
  }

  // ─── Happy path ────────────────────────────────────────────────────────────

  @Test
  void shouldPass_WhenBusinessUnitCodeDoesNotExist() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null);

    assertDoesNotThrow(() -> validator.validate(warehouse));
  }

  // ─── Failure: duplicate code ───────────────────────────────────────────────

  @Test
  void shouldThrowBadRequest_WhenBusinessUnitCodeAlreadyExists() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> validator.validate(warehouse));
    assertTrue(ex.getMessage().contains("MWH.001"));
    assertTrue(ex.getMessage().contains("already exists"));
  }
}
