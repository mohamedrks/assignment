package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReplaceWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private WarehouseValidator validator;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    validator = mock(WarehouseValidator.class);
    useCase = new ReplaceWarehouseUseCase(warehouseStore, validator);
  }

  @Test
  void shouldReplaceWarehouseSuccessfully() {
    // given
    Warehouse existing = buildActiveWarehouse("MWH.001", "ZWOLLE-001", 100, 10);
    Warehouse newWarehouse = buildActiveWarehouse("MWH.001", "ZWOLLE-001", 50, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    // when
    useCase.replace(newWarehouse);

    // then - old warehouse archived, new one created
    assertNotNull(existing.archivedAt);
    assertNotNull(newWarehouse.createdAt);
    verify(validator).validateNotArchived(existing);
    verify(validator).validateLocationExists("ZWOLLE-001");
    verify(warehouseStore).update(existing);
    verify(warehouseStore).create(newWarehouse);
  }

  @Test
  void shouldRejectWhenWarehouseDoesNotExist() {
    // given
    Warehouse newWarehouse = buildActiveWarehouse("MWH.999", "ZWOLLE-001", 50, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(null);

    // when / then
    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> useCase.replace(newWarehouse));
    assertEquals(404, ex.getResponse().getStatus());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectWhenWarehouseAlreadyArchived() {
    // given
    Warehouse existing = buildActiveWarehouse("MWH.001", "ZWOLLE-001", 100, 10);
    existing.archivedAt = LocalDateTime.now().minusDays(1);
    Warehouse newWarehouse = buildActiveWarehouse("MWH.001", "ZWOLLE-001", 50, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    doThrow(new WebApplicationException(400)).when(validator).validateNotArchived(existing);

    // when / then
    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> useCase.replace(newWarehouse));
    assertEquals(400, ex.getResponse().getStatus());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectWhenNewLocationIsInvalid() {
    // given
    Warehouse existing = buildActiveWarehouse("MWH.001", "ZWOLLE-001", 100, 10);
    Warehouse newWarehouse = buildActiveWarehouse("MWH.001", "INVALID-001", 50, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    doThrow(new WebApplicationException(400)).when(validator).validateLocationExists("INVALID-001");

    // when / then
    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> useCase.replace(newWarehouse));
    assertEquals(400, ex.getResponse().getStatus());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectWhenNewCapacityCannotAccommodateExistingStock() {
    // given
    Warehouse existing = buildActiveWarehouse("MWH.001", "ZWOLLE-001", 100, 30);
    Warehouse newWarehouse = buildActiveWarehouse("MWH.001", "ZWOLLE-001", 20, 30); // capacity 20 < stock 30

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    // when / then
    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> useCase.replace(newWarehouse));
    assertEquals(400, ex.getResponse().getStatus());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectWhenStockDoesNotMatch() {
    // given
    Warehouse existing = buildActiveWarehouse("MWH.001", "ZWOLLE-001", 100, 10);
    Warehouse newWarehouse = buildActiveWarehouse("MWH.001", "ZWOLLE-001", 50, 99); // stock mismatch

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    // when / then
    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> useCase.replace(newWarehouse));
    assertEquals(400, ex.getResponse().getStatus());
    verify(warehouseStore, never()).create(any());
  }

  private Warehouse buildActiveWarehouse(String code, String location, int capacity, int stock) {
    Warehouse w = new Warehouse();
    w.businessUnitCode = code;
    w.location = location;
    w.capacity = capacity;
    w.stock = stock;
    w.archivedAt = null;
    return w;
  }
}
