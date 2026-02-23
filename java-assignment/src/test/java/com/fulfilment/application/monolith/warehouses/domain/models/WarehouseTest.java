package com.fulfilment.application.monolith.warehouses.domain.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class WarehouseTest {

  @Test
  void defaultConstructorShouldCreateEmptyWarehouse() {
    Warehouse w = new Warehouse();

    assertNull(w.id);
    assertNull(w.businessUnitCode);
    assertNull(w.location);
    assertNull(w.capacity);
    assertNull(w.stock);
    assertNull(w.createdAt);
    assertNull(w.archivedAt);
  }

  @Test
  void fieldsShouldBeAssignable() {
    LocalDateTime now = LocalDateTime.now();
    Warehouse w = new Warehouse();
    w.id = 1L;
    w.businessUnitCode = "MWH.001";
    w.location = "AMSTERDAM-001";
    w.capacity = 100;
    w.stock = 50;
    w.createdAt = now;
    w.archivedAt = null;

    assertEquals(1L, w.id);
    assertEquals("MWH.001", w.businessUnitCode);
    assertEquals("AMSTERDAM-001", w.location);
    assertEquals(100, w.capacity);
    assertEquals(50, w.stock);
    assertEquals(now, w.createdAt);
    assertNull(w.archivedAt);
  }

  @Test
  void isArchivedWhenArchivedAtIsSet() {
    Warehouse w = new Warehouse();
    w.archivedAt = LocalDateTime.now().minusDays(1);

    assertNotNull(w.archivedAt);
  }

  @Test
  void isNotArchivedWhenArchivedAtIsNull() {
    Warehouse w = new Warehouse();
    w.archivedAt = null;

    assertNull(w.archivedAt);
  }
}
