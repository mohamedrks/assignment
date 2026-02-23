package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ArchiveWarehouseUseCase.class.getName());

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator validator;

  @Inject
  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.validator = validator;
  }

  @Override
  public void archive(Warehouse warehouse) {
    LOGGER.infof("Archiving warehouse businessUnitCode=%s", warehouse.businessUnitCode);

    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing == null) {
      LOGGER.warnf("Warehouse not found for archiving: businessUnitCode=%s", warehouse.businessUnitCode);
      throw new WebApplicationException(
          "Warehouse with business unit code " + warehouse.businessUnitCode + " does not exist.", 404);
    }

    validator.validateNotArchived(existing);

    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);
    LOGGER.infof("Warehouse archived id=%s businessUnitCode=%s", existing.id, existing.businessUnitCode);
  }
}
