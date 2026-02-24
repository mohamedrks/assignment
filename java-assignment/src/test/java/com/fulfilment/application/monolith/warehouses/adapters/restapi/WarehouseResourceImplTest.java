package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WarehouseResourceImplTest {

  @Mock private WarehouseStore warehouseStore;
  @Mock private CreateWarehouseOperation createWarehouseOperation;
  @Mock private ReplaceWarehouseOperation replaceWarehouseOperation;
  @Mock private ArchiveWarehouseOperation archiveWarehouseOperation;

  @InjectMocks private WarehouseResourceImpl resource;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  // --- listAllWarehousesUnits ---

  @Test
  void listAllShouldReturnOnlyActiveWarehouses() {
    Warehouse active = buildWarehouse(1L, "MWH.001", "AMSTERDAM-001", 100, 50, false);
    Warehouse archived = buildWarehouse(2L, "MWH.002", "ZWOLLE-001", 40, 10, true);
    when(warehouseStore.getAll()).thenReturn(List.of(active, archived));

    var result = resource.listAllWarehousesUnits();

    assertEquals(1, result.size());
    assertEquals("MWH.001", result.get(0).getBusinessUnitCode());
  }

  @Test
  void listAllShouldReturnEmptyWhenNoActiveWarehouses() {
    Warehouse archived = buildWarehouse(1L, "MWH.001", "AMSTERDAM-001", 100, 50, true);
    when(warehouseStore.getAll()).thenReturn(List.of(archived));

    var result = resource.listAllWarehousesUnits();

    assertTrue(result.isEmpty());
  }

  @Test
  void listAllShouldReturnEmptyWhenStoreIsEmpty() {
    when(warehouseStore.getAll()).thenReturn(List.of());

    var result = resource.listAllWarehousesUnits();

    assertTrue(result.isEmpty());
  }

  // --- getAWarehouseUnitByID ---

  @Test
  void getByIdShouldReturnWarehouseWhenFound() {
    Warehouse warehouse = buildWarehouse(1L, "MWH.001", "AMSTERDAM-001", 100, 50, false);
    when(warehouseStore.findWarehouseById(1L)).thenReturn(warehouse);

    var result = resource.getAWarehouseUnitByID("1");

    assertEquals("MWH.001", result.getBusinessUnitCode());
    assertEquals("AMSTERDAM-001", result.getLocation());
    assertEquals(100, result.getCapacity());
  }

  @Test
  void getByIdShouldThrow404WhenNotFound() {
    when(warehouseStore.findWarehouseById(99L)).thenReturn(null);

    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> resource.getAWarehouseUnitByID("99"));
    assertEquals(404, ex.getResponse().getStatus());
  }

  // --- createANewWarehouseUnit ---

  @Test
  void createShouldDelegateToUseCaseAndReturnWarehouse() {
    com.warehouse.api.beans.Warehouse input = new com.warehouse.api.beans.Warehouse();
    input.setBusinessUnitCode("MWH.NEW");
    input.setLocation("AMSTERDAM-001");
    input.setCapacity(50);
    input.setStock(0);

    var result = resource.createANewWarehouseUnit(input);

    verify(createWarehouseOperation).create(any());
    assertEquals("MWH.NEW", result.getBusinessUnitCode());
    assertEquals("AMSTERDAM-001", result.getLocation());
  }

  // --- archiveAWarehouseUnitByID ---

  @Test
  void archiveShouldDelegateToUseCaseWhenFound() {
    Warehouse warehouse = buildWarehouse(1L, "MWH.001", "AMSTERDAM-001", 100, 50, false);
    when(warehouseStore.findWarehouseById(1L)).thenReturn(warehouse);

    assertDoesNotThrow(() -> resource.archiveAWarehouseUnitByID("1"));
    verify(archiveWarehouseOperation).archive(warehouse);
  }

  @Test
  void archiveShouldThrow404WhenNotFound() {
    when(warehouseStore.findWarehouseById(99L)).thenReturn(null);

    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> resource.archiveAWarehouseUnitByID("99"));
    assertEquals(404, ex.getResponse().getStatus());
  }

  // --- replaceTheCurrentActiveWarehouse ---

  @Test
  void replaceShouldDelegateToUseCaseWithBusinessUnitCode() {
    com.warehouse.api.beans.Warehouse input = new com.warehouse.api.beans.Warehouse();
    input.setLocation("ZWOLLE-001");
    input.setCapacity(40);
    input.setStock(10);

    var result = resource.replaceTheCurrentActiveWarehouse("MWH.001", input);

    verify(replaceWarehouseOperation).replace(any());
    assertEquals("MWH.001", result.getBusinessUnitCode());
  }

  // Helper

  private Warehouse buildWarehouse(Long id, String code, String location, int capacity, int stock, boolean archived) {
    Warehouse w = new Warehouse();
    w.id = id;
    w.businessUnitCode = code;
    w.location = location;
    w.capacity = capacity;
    w.stock = stock;
    w.archivedAt = archived ? java.time.LocalDateTime.now().minusDays(1) : null;
    return w;
  }
}
