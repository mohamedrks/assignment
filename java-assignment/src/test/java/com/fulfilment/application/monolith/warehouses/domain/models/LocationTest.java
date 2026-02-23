package com.fulfilment.application.monolith.warehouses.domain.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocationTest {

  @Test
  void constructorShouldSetAllFields() {
    Location location = new Location("AMSTERDAM-001", 5, 100);

    assertEquals("AMSTERDAM-001", location.identification);
    assertEquals(5, location.maxNumberOfWarehouses);
    assertEquals(100, location.maxCapacity);
  }

  @Test
  void constructorShouldSetSingleWarehouseLocation() {
    Location location = new Location("ZWOLLE-001", 1, 40);

    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  void fieldsShouldBeMutable() {
    Location location = new Location("TILBURG-001", 1, 40);
    location.identification = "TILBURG-002";
    location.maxNumberOfWarehouses = 2;
    location.maxCapacity = 80;

    assertEquals("TILBURG-002", location.identification);
    assertEquals(2, location.maxNumberOfWarehouses);
    assertEquals(80, location.maxCapacity);
  }
}
