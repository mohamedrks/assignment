package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ReplaceWarehouseUseCase.class.getName());

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator validator;

  @Inject
  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.validator = validator;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    LOGGER.infof("Replacing warehouse businessUnitCode=%s with location=%s capacity=%d",
        newWarehouse.businessUnitCode, newWarehouse.location, newWarehouse.capacity);

    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      LOGGER.warnf("Warehouse not found for replacement: businessUnitCode=%s", newWarehouse.businessUnitCode);
      throw new WebApplicationException(
          "Warehouse with business unit code " + newWarehouse.businessUnitCode + " does not exist.", 404);
    }

    validator.validateNotArchived(existing);

    validator.validateLocationExists(newWarehouse.location);

    // New warehouse capacity must accommodate the existing warehouse's stock
    if (newWarehouse.capacity < existing.stock) {
      throw new WebApplicationException(
          "New warehouse capacity must be able to accommodate the current stock of " + existing.stock + ".", 400);
    }

    // Stock of new warehouse must match existing warehouse stock
    if (!newWarehouse.stock.equals(existing.stock)) {
      throw new WebApplicationException(
          "New warehouse stock must match the existing warehouse stock of " + existing.stock + ".", 400);
    }

    // Archive the old warehouse
    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);
    LOGGER.infof("Archived old warehouse id=%s businessUnitCode=%s", existing.id, existing.businessUnitCode);

    // Create the new warehouse
    newWarehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(newWarehouse);
    LOGGER.infof("Created replacement warehouse id=%s businessUnitCode=%s", newWarehouse.id, newWarehouse.businessUnitCode);
  }
}
