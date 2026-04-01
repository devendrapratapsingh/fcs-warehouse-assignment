package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidLocationValidatorTest {

  @Mock LocationResolver locationResolver;
  @InjectMocks ValidLocationValidator validator;

  private Warehouse warehouse;

  @BeforeEach
  void setUp() {
    warehouse = new Warehouse();
    warehouse.location = "AMSTERDAM";
  }

  // ─── Happy path ────────────────────────────────────────────────────────────

  @Test
  void shouldPass_WhenLocationExists() {
    when(locationResolver.resolveByIdentifier("AMSTERDAM"))
        .thenReturn(new Location("AMSTERDAM", 3, 500));

    assertDoesNotThrow(() -> validator.validate(warehouse));
  }

  // ─── Failure: NotFoundException from resolver ──────────────────────────────

  @Test
  void shouldThrowBadRequest_WhenLocationResolverThrowsNotFoundException() {
    when(locationResolver.resolveByIdentifier("AMSTERDAM"))
        .thenThrow(new NotFoundException("not found"));

    // NotFoundException propagates unchanged (it IS-A WebApplicationException)
    assertThrows(NotFoundException.class, () -> validator.validate(warehouse));
  }

  // ─── Failure: IllegalArgumentException from resolver ──────────────────────

  @Test
  void shouldThrowBadRequest_WhenLocationResolverThrowsIllegalArgumentException() {
    when(locationResolver.resolveByIdentifier("AMSTERDAM"))
        .thenThrow(new IllegalArgumentException("bad arg"));

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> validator.validate(warehouse));
    assertTrue(ex.getMessage().contains("AMSTERDAM"));
    assertTrue(ex.getMessage().contains("not valid"));
  }

  // ─── Failure: resolver returns null ───────────────────────────────────────

  @Test
  void shouldThrowBadRequest_WhenLocationResolverReturnsNull() {
    when(locationResolver.resolveByIdentifier("AMSTERDAM")).thenReturn(null);

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> validator.validate(warehouse));
    assertTrue(ex.getMessage().contains("AMSTERDAM"));
    assertTrue(ex.getMessage().contains("not valid"));
  }
}
