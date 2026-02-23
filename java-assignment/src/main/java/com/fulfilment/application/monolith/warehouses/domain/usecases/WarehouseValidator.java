package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@ApplicationScoped
public class WarehouseValidator {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @Inject
  public WarehouseValidator(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  public void validateBusinessUnitCodeIsUnique(String businessUnitCode) {
    if (warehouseStore.findByBusinessUnitCode(businessUnitCode) != null) {
      throw new WebApplicationException(
          "Warehouse with business unit code " + businessUnitCode + " already exists.", 400);
    }
  }

  public Location validateLocationExists(String locationIdentifier) {
    Location location = locationResolver.resolveByIdentifier(locationIdentifier);
    if (location == null) {
      throw new WebApplicationException(
          "Location " + locationIdentifier + " does not exist.", 400);
    }
    return location;
  }

  public void validateNotArchived(Warehouse warehouse) {
    if (warehouse.archivedAt != null) {
      throw new WebApplicationException(
          "Warehouse with business unit code " + warehouse.businessUnitCode + " is already archived.", 400);
    }
  }

  public void validateLocationCapacity(String locationIdentifier, Location location, int additionalCapacity) {
    List<Warehouse> activeAtLocation = warehouseStore.getAll().stream()
        .filter(w -> w.location.equals(locationIdentifier) && w.archivedAt == null)
        .toList();

    if (activeAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new WebApplicationException(
          "Location " + locationIdentifier + " has reached its maximum number of warehouses.", 400);
    }

    int totalCapacity = activeAtLocation.stream().mapToInt(w -> w.capacity).sum();
    if (totalCapacity + additionalCapacity > location.maxCapacity) {
      throw new WebApplicationException(
          "Warehouse capacity exceeds the maximum allowed capacity for location " + locationIdentifier + ".", 400);
    }
  }
}
