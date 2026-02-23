package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CreateWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private WarehouseValidator validator;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    validator = mock(WarehouseValidator.class);
    useCase = new CreateWarehouseUseCase(warehouseStore, validator);
  }

  @Test
  void shouldCreateWarehouseSuccessfully() {
    // given
    Warehouse warehouse = buildWarehouse("MWH.NEW", "AMSTERDAM-001", 20, 0);
    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(validator.validateLocationExists("AMSTERDAM-001")).thenReturn(location);

    // when
    useCase.create(warehouse);

    // then
    verify(validator).validateBusinessUnitCodeIsUnique("MWH.NEW");
    verify(validator).validateLocationExists("AMSTERDAM-001");
    verify(validator).validateLocationCapacity("AMSTERDAM-001", location, 20);
    verify(warehouseStore).create(warehouse);
    assertNotNull(warehouse.createdAt);
  }

  @Test
  void shouldRejectDuplicateBusinessUnitCode() {
    // given
    Warehouse warehouse = buildWarehouse("MWH.001", "AMSTERDAM-001", 20, 0);
    doThrow(new WebApplicationException(400))
        .when(validator).validateBusinessUnitCodeIsUnique("MWH.001");

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
    doThrow(new WebApplicationException(400))
        .when(validator).validateLocationExists("INVALID-001");

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
    Location location = new Location("ZWOLLE-001", 1, 40);

    when(validator.validateLocationExists("ZWOLLE-001")).thenReturn(location);
    doThrow(new WebApplicationException(400))
        .when(validator).validateLocationCapacity("ZWOLLE-001", location, 10);

    // when / then
    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> useCase.create(warehouse));
    assertEquals(400, ex.getResponse().getStatus());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectWhenCapacityExceedsLocationMax() {
    // given
    Warehouse warehouse = buildWarehouse("MWH.NEW", "ZWOLLE-001", 40, 0);
    Location location = new Location("ZWOLLE-001", 2, 40);

    when(validator.validateLocationExists("ZWOLLE-001")).thenReturn(location);
    doThrow(new WebApplicationException(400))
        .when(validator).validateLocationCapacity("ZWOLLE-001", location, 40);

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
