package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // Find the existing active warehouse by business unit code
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new WebApplicationException(
          "Warehouse with business unit code " + newWarehouse.businessUnitCode + " does not exist.", 404);
    }
    if (existing.archivedAt != null) {
      throw new WebApplicationException(
          "Warehouse with business unit code " + newWarehouse.businessUnitCode + " is already archived.", 400);
    }

    // Validate new location exists
    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new WebApplicationException("Location " + newWarehouse.location + " does not exist.", 400);
    }

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

    // Create the new warehouse
    newWarehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(newWarehouse);
  }
}
