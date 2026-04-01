package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class LocationGatewayTest {

  @Inject
  LocationGateway locationGateway;

  @Test
  public void testResolveExistingLocation() {
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");
    assertEquals("ZWOLLE-001", location.getIdentification());
  }

  @Test
  public void testResolveLocationIsCaseInsensitive() {
    Location location = locationGateway.resolveByIdentifier("zwolle-001");
    assertEquals("ZWOLLE-001", location.getIdentification());
  }

  @Test
  public void testResolveUnknownLocationThrowsNotFoundException() {
    assertThrows(NotFoundException.class,
        () -> locationGateway.resolveByIdentifier("DOES-NOT-EXIST"));
  }
}
