package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class.getName());

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator validator;

  @Inject
  public CreateWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.validator = validator;
  }

  @Override
  public void create(Warehouse warehouse) {
    LOGGER.infof("Creating warehouse businessUnitCode=%s location=%s capacity=%d",
        warehouse.businessUnitCode, warehouse.location, warehouse.capacity);

    validator.validateBusinessUnitCodeIsUnique(warehouse.businessUnitCode);

    Location location = validator.validateLocationExists(warehouse.location);

    validator.validateLocationCapacity(warehouse.location, location, warehouse.capacity);

    warehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(warehouse);
    LOGGER.infof("Warehouse created id=%s businessUnitCode=%s", warehouse.id, warehouse.businessUnitCode);
  }
}
