package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.jboss.logging.Logger;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class.getName());

  @Inject private WarehouseStore warehouseStore;
  @Inject private CreateWarehouseOperation createWarehouseOperation;
  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;
  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseStore.getAll().stream()
        .filter(w -> w.archivedAt == null)
        .map(this::toWarehouseResponse)
        .toList();
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    LOGGER.infof("Creating warehouse with businessUnitCode=%s", data.getBusinessUnitCode());
    var warehouse = toDomainWarehouse(data);
    createWarehouseOperation.create(warehouse);
    return toWarehouseResponse(warehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var warehouse = warehouseStore.findWarehouseById(Long.parseLong(id));
    if (warehouse == null) {
      LOGGER.warnf("Warehouse not found: %s", id);
      throw new WebApplicationException("Warehouse with id " + id + " does not exist.", 404);
    }
    return toWarehouseResponse(warehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    LOGGER.infof("Archiving warehouse with id=%s", id);
    var warehouse = warehouseStore.findWarehouseById(Long.parseLong(id));
    if (warehouse == null) {
      LOGGER.warnf("Warehouse not found for archiving: %s", id);
      throw new WebApplicationException("Warehouse with id " + id + " does not exist.", 404);
    }
    archiveWarehouseOperation.archive(warehouse);
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    LOGGER.infof("Replacing warehouse with businessUnitCode=%s", businessUnitCode);
    var newWarehouse = toDomainWarehouse(data);
    newWarehouse.businessUnitCode = businessUnitCode;
    replaceWarehouseOperation.replace(newWarehouse);
    return toWarehouseResponse(newWarehouse);
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainWarehouse(
      Warehouse data) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = data.getBusinessUnitCode();
    warehouse.location = data.getLocation();
    warehouse.capacity = data.getCapacity();
    warehouse.stock = data.getStock();
    return warehouse;
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setId(warehouse.id != null ? String.valueOf(warehouse.id) : null);
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);
    return response;
  }
}
