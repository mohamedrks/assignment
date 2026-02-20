package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    // Validate business unit code doesn't already exist
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new WebApplicationException(
          "Warehouse with business unit code " + warehouse.businessUnitCode + " already exists.", 400);
    }

    // Validate location exists
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new WebApplicationException("Location " + warehouse.location + " does not exist.", 400);
    }

    // Validate max warehouses per location not exceeded
    List<Warehouse> warehousesAtLocation = warehouseStore.getAll().stream()
        .filter(w -> w.location.equals(warehouse.location) && w.archivedAt == null)
        .toList();
    if (warehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new WebApplicationException(
          "Location " + warehouse.location + " has reached its maximum number of warehouses.", 400);
    }

    // Validate capacity does not exceed location max capacity
    int totalCapacity = warehousesAtLocation.stream().mapToInt(w -> w.capacity).sum();
    if (totalCapacity + warehouse.capacity > location.maxCapacity) {
      throw new WebApplicationException(
          "Warehouse capacity exceeds the maximum allowed capacity for location " + warehouse.location + ".", 400);
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(warehouse);
  }
}
