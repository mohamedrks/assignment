package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WarehouseValidatorTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private WarehouseValidator validator;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    validator = new WarehouseValidator(warehouseStore, locationResolver);
  }

  // --- validateBusinessUnitCodeIsUnique ---

  @Test
  void shouldPassWhenBusinessUnitCodeIsUnique() {
    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    assertDoesNotThrow(() -> validator.validateBusinessUnitCodeIsUnique("MWH.NEW"));
  }

  @Test
  void shouldThrow400WhenBusinessUnitCodeAlreadyExists() {
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 50, 10, null);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> validator.validateBusinessUnitCodeIsUnique("MWH.001"));
    assertEquals(400, ex.getResponse().getStatus());
  }

  // --- validateLocationExists ---

  @Test
  void shouldReturnLocationWhenItExists() {
    Location location = new Location("AMSTERDAM-001", 5, 100);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);

    Location result = validator.validateLocationExists("AMSTERDAM-001");
    assertEquals("AMSTERDAM-001", result.identification);
  }

  @Test
  void shouldThrow400WhenLocationDoesNotExist() {
    when(locationResolver.resolveByIdentifier("INVALID-001")).thenReturn(null);

    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> validator.validateLocationExists("INVALID-001"));
    assertEquals(400, ex.getResponse().getStatus());
  }

  // --- validateNotArchived ---

  @Test
  void shouldPassWhenWarehouseIsNotArchived() {
    Warehouse warehouse = buildWarehouse("MWH.001", "ZWOLLE-001", 50, 10, null);
    assertDoesNotThrow(() -> validator.validateNotArchived(warehouse));
  }

  @Test
  void shouldThrow400WhenWarehouseIsAlreadyArchived() {
    Warehouse warehouse = buildWarehouse("MWH.001", "ZWOLLE-001", 50, 10, LocalDateTime.now().minusDays(1));

    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> validator.validateNotArchived(warehouse));
    assertEquals(400, ex.getResponse().getStatus());
  }

  // --- validateLocationCapacity ---

  @Test
  void shouldPassWhenLocationHasCapacityAndSlots() {
    Location location = new Location("AMSTERDAM-001", 5, 100);
    when(warehouseStore.getAll()).thenReturn(List.of());

    assertDoesNotThrow(() -> validator.validateLocationCapacity("AMSTERDAM-001", location, 30));
  }

  @Test
  void shouldThrow400WhenMaxWarehousesPerLocationReached() {
    Location location = new Location("ZWOLLE-001", 1, 40);
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 20, 5, null);
    when(warehouseStore.getAll()).thenReturn(List.of(existing));

    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> validator.validateLocationCapacity("ZWOLLE-001", location, 10));
    assertEquals(400, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("maximum number of warehouses"));
  }

  @Test
  void shouldThrow400WhenCapacityExceedsLocationMax() {
    Location location = new Location("AMSTERDAM-001", 5, 100);
    Warehouse existing = buildWarehouse("MWH.001", "AMSTERDAM-001", 80, 10, null);
    when(warehouseStore.getAll()).thenReturn(List.of(existing));

    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> validator.validateLocationCapacity("AMSTERDAM-001", location, 30));
    assertEquals(400, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("maximum allowed capacity"));
  }

  @Test
  void shouldNotCountArchivedWarehousesInCapacityCheck() {
    Location location = new Location("AMSTERDAM-001", 5, 100);
    Warehouse archived = buildWarehouse("MWH.001", "AMSTERDAM-001", 80, 10, LocalDateTime.now().minusDays(1));
    when(warehouseStore.getAll()).thenReturn(List.of(archived));

    // archived warehouse's capacity should not count — 20 capacity should pass
    assertDoesNotThrow(() -> validator.validateLocationCapacity("AMSTERDAM-001", location, 20));
  }

  private Warehouse buildWarehouse(String code, String location, int capacity, int stock, LocalDateTime archivedAt) {
    Warehouse w = new Warehouse();
    w.businessUnitCode = code;
    w.location = location;
    w.capacity = capacity;
    w.stock = stock;
    w.archivedAt = archivedAt;
    return w;
  }
}
