package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ArchiveWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private WarehouseValidator validator;
  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    validator = mock(WarehouseValidator.class);
    useCase = new ArchiveWarehouseUseCase(warehouseStore, validator);
  }

  @Test
  void shouldArchiveWarehouseSuccessfully() {
    // given
    Warehouse warehouse = buildActiveWarehouse("MWH.001");
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(warehouse);

    // when
    useCase.archive(warehouse);

    // then
    assertNotNull(warehouse.archivedAt);
    verify(validator).validateNotArchived(warehouse);
    verify(warehouseStore).update(warehouse);
  }

  @Test
  void shouldRejectArchivingNonExistentWarehouse() {
    // given
    Warehouse warehouse = buildActiveWarehouse("MWH.999");
    when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(null);

    // when / then
    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> useCase.archive(warehouse));
    assertEquals(404, ex.getResponse().getStatus());
    verify(warehouseStore, never()).update(any());
  }

  @Test
  void shouldRejectArchivingAlreadyArchivedWarehouse() {
    // given
    Warehouse warehouse = buildActiveWarehouse("MWH.001");
    warehouse.archivedAt = LocalDateTime.now().minusDays(1);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(warehouse);
    doThrow(new WebApplicationException(400)).when(validator).validateNotArchived(warehouse);

    // when / then
    WebApplicationException ex = assertThrows(WebApplicationException.class,
        () -> useCase.archive(warehouse));
    assertEquals(400, ex.getResponse().getStatus());
    verify(warehouseStore, never()).update(any());
  }

  private Warehouse buildActiveWarehouse(String code) {
    Warehouse w = new Warehouse();
    w.businessUnitCode = code;
    w.location = "AMSTERDAM-001";
    w.capacity = 50;
    w.stock = 10;
    w.archivedAt = null;
    return w;
  }
}
