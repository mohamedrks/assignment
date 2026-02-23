package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LocationGatewayTest {

  private final LocationGateway locationGateway = new LocationGateway();

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");
    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  public void testWhenResolveNonExistingLocationShouldReturnNull() {
    assertNull(locationGateway.resolveByIdentifier("UNKNOWN-001"));
  }

  @Test
  public void testResolveZwolle002() {
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-002");
    assertEquals("ZWOLLE-002", location.identification);
    assertEquals(2, location.maxNumberOfWarehouses);
    assertEquals(50, location.maxCapacity);
  }

  @Test
  public void testResolveAmsterdam001() {
    Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");
    assertEquals("AMSTERDAM-001", location.identification);
    assertEquals(5, location.maxNumberOfWarehouses);
    assertEquals(100, location.maxCapacity);
  }

  @Test
  public void testResolveAmsterdam002() {
    Location location = locationGateway.resolveByIdentifier("AMSTERDAM-002");
    assertEquals("AMSTERDAM-002", location.identification);
    assertEquals(3, location.maxNumberOfWarehouses);
    assertEquals(75, location.maxCapacity);
  }

  @Test
  public void testResolveTilburg001() {
    Location location = locationGateway.resolveByIdentifier("TILBURG-001");
    assertEquals("TILBURG-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  public void testResolveHelmond001() {
    Location location = locationGateway.resolveByIdentifier("HELMOND-001");
    assertEquals("HELMOND-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(45, location.maxCapacity);
  }

  @Test
  public void testResolveEindhoven001() {
    Location location = locationGateway.resolveByIdentifier("EINDHOVEN-001");
    assertEquals("EINDHOVEN-001", location.identification);
    assertEquals(2, location.maxNumberOfWarehouses);
    assertEquals(70, location.maxCapacity);
  }

  @Test
  public void testResolveVetsby001() {
    Location location = locationGateway.resolveByIdentifier("VETSBY-001");
    assertEquals("VETSBY-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(90, location.maxCapacity);
  }
}
