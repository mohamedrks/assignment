package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class DbWarehouseTest {

  @Test
  void toWarehouseShouldMapAllFields() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime archived = now.plusDays(1);

    DbWarehouse db = new DbWarehouse();
    db.id = 42L;
    db.businessUnitCode = "MWH.001";
    db.location = "AMSTERDAM-001";
    db.capacity = 100;
    db.stock = 50;
    db.createdAt = now;
    db.archivedAt = archived;

    Warehouse w = db.toWarehouse();

    assertEquals(42L, w.id);
    assertEquals("MWH.001", w.businessUnitCode);
    assertEquals("AMSTERDAM-001", w.location);
    assertEquals(100, w.capacity);
    assertEquals(50, w.stock);
    assertEquals(now, w.createdAt);
    assertEquals(archived, w.archivedAt);
  }

  @Test
  void toWarehouseShouldHandleNullFields() {
    DbWarehouse db = new DbWarehouse();

    Warehouse w = db.toWarehouse();

    assertNull(w.id);
    assertNull(w.businessUnitCode);
    assertNull(w.location);
    assertNull(w.capacity);
    assertNull(w.stock);
    assertNull(w.createdAt);
    assertNull(w.archivedAt);
  }

  @Test
  void toWarehouseShouldHandleNullArchivedAt() {
    DbWarehouse db = new DbWarehouse();
    db.id = 1L;
    db.businessUnitCode = "MWH.002";
    db.location = "ZWOLLE-001";
    db.capacity = 40;
    db.stock = 10;
    db.createdAt = LocalDateTime.now();
    db.archivedAt = null;

    Warehouse w = db.toWarehouse();

    assertNull(w.archivedAt);
    assertEquals("MWH.002", w.businessUnitCode);
  }

  @Test
  void defaultConstructorShouldCreateEmptyInstance() {
    DbWarehouse db = new DbWarehouse();
    assertNull(db.id);
    assertNull(db.businessUnitCode);
    assertNull(db.location);
    assertNull(db.capacity);
    assertNull(db.stock);
    assertNull(db.createdAt);
    assertNull(db.archivedAt);
  }
}
