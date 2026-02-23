package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ResponseStatus;

/**
 * Implements the Warehouse REST API as defined in warehouse-openapi.yaml.
 *
 * <p>Note: does not implement the generated WarehouseResource interface because
 * quarkus-openapi-generator-server 2.4.7 does not support emitting a non-200 status code on POST
 * (upstream issue #670). All JAX-RS annotations mirror the OpenAPI spec exactly.
 */
@Path("/warehouse")
@RequestScoped
public class WarehouseResourceImpl {

  private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class.getName());

  @Inject private WarehouseStore warehouseStore;
  @Inject private CreateWarehouseOperation createWarehouseOperation;
  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;
  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;

  @GET
  @Produces("application/json")
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseStore.getAll().stream()
        .filter(w -> w.archivedAt == null)
        .map(this::toWarehouseResponse)
        .toList();
  }

  @POST
  @Produces("application/json")
  @Consumes("application/json")
  @Transactional
  @ResponseStatus(201)
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    LOGGER.infof("Creating warehouse with businessUnitCode=%s", data.getBusinessUnitCode());
    var warehouse = toDomainWarehouse(data);
    createWarehouseOperation.create(warehouse);
    return toWarehouseResponse(warehouse);
  }

  @Path("/{id}")
  @GET
  @Produces("application/json")
  public Warehouse getAWarehouseUnitByID(@PathParam("id") String id) {
    var warehouse = warehouseStore.findWarehouseById(Long.parseLong(id));
    if (warehouse == null) {
      LOGGER.warnf("Warehouse not found: %s", id);
      throw new WebApplicationException("Warehouse with id " + id + " does not exist.", 404);
    }
    return toWarehouseResponse(warehouse);
  }

  @Path("/{id}")
  @DELETE
  @Transactional
  public void archiveAWarehouseUnitByID(@PathParam("id") String id) {
    LOGGER.infof("Archiving warehouse with id=%s", id);
    var warehouse = warehouseStore.findWarehouseById(Long.parseLong(id));
    if (warehouse == null) {
      LOGGER.warnf("Warehouse not found for archiving: %s", id);
      throw new WebApplicationException("Warehouse with id " + id + " does not exist.", 404);
    }
    archiveWarehouseOperation.archive(warehouse);
  }

  @Path("/{businessUnitCode}/replacement")
  @POST
  @Produces("application/json")
  @Consumes("application/json")
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      @PathParam("businessUnitCode") String businessUnitCode, @NotNull Warehouse data) {
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
