package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CreateWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void shouldCreateWarehouseSuccessfully() {
    // given
    Warehouse warehouse = buildWarehouse("MWH.NEW", "AMSTERDAM-001", 20, 0);
    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseStore.getAll()).thenReturn(List.of());

    // when
    useCase.create(warehouse);

    // then
    verify(warehouseStore).create(warehouse);
    assertNotNull(warehouse.createdAt);
  }

  @Test
  void shouldRejectDuplicateBusinessUnitCode() {
    // given
    Warehouse warehouse = buildWarehouse("MWH.001", "AMSTERDAM-001", 20, 0);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(warehouse);

    // when / then
    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> useCase.create(warehouse));
    assertEquals(400, ex.getResponse().getStatus());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectInvalidLocation() {
    // given
    Warehouse warehouse = buildWarehouse("MWH.NEW", "INVALID-001", 20, 0);
    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("INVALID-001")).thenReturn(null);

    // when / then
    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> useCase.create(warehouse));
    assertEquals(400, ex.getResponse().getStatus());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectWhenMaxWarehousesPerLocationReached() {
    // given
    Warehouse warehouse = buildWarehouse("MWH.NEW", "ZWOLLE-001", 10, 0);
    Location location = new Location("ZWOLLE-001", 1, 40); // max 1 warehouse

    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 10, 5);
    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseStore.getAll()).thenReturn(List.of(existing)); // already 1

    // when / then
    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> useCase.create(warehouse));
    assertEquals(400, ex.getResponse().getStatus());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectWhenCapacityExceedsLocationMax() {
    // given
    Warehouse warehouse = buildWarehouse("MWH.NEW", "ZWOLLE-001", 40, 0); // capacity 40
    Location location = new Location("ZWOLLE-001", 2, 40); // max total capacity 40

    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 5); // already 30 used
    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseStore.getAll()).thenReturn(List.of(existing));

    // when / then
    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> useCase.create(warehouse));
    assertEquals(400, ex.getResponse().getStatus());
    verify(warehouseStore, never()).create(any());
  }

  private Warehouse buildWarehouse(String code, String location, int capacity, int stock) {
    Warehouse w = new Warehouse();
    w.businessUnitCode = code;
    w.location = location;
    w.capacity = capacity;
    w.stock = stock;
    return w;
  }
}
