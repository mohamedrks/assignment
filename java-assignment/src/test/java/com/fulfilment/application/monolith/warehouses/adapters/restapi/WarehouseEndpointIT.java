package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseEndpointIT {

  // -------------------------
  // GET /warehouse
  // -------------------------

  @Test
  @Order(1)
  public void testSimpleListWarehouses() {
    given()
        .when()
        .get("warehouse")
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  // -------------------------
  // GET /warehouse/{id}
  // -------------------------

  @Test
  @Order(2)
  public void testGetWarehouseById() {
    given()
        .when()
        .get("warehouse/2")
        .then()
        .statusCode(200)
        .body(containsString("MWH.012"), containsString("AMSTERDAM-001"));
  }

  @Test
  @Order(3)
  public void testGetNonExistentWarehouseReturns404() {
    given()
        .when()
        .get("warehouse/9999")
        .then()
        .statusCode(404);
  }

  // -------------------------
  // POST /warehouse
  // -------------------------

  @Test
  @Order(4)
  public void testCreateWarehouse() {
    String body = "{\"businessUnitCode\": \"MWH.NEW\", \"location\": \"AMSTERDAM-001\", \"capacity\": 10, \"stock\": 0}";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("warehouse")
        .then()
        .statusCode(201)
        .body(containsString("MWH.NEW"));

    given()
        .when()
        .get("warehouse")
        .then()
        .statusCode(200)
        .body(containsString("MWH.NEW"));
  }

  @Test
  @Order(5)
  public void testCreateWarehouseWithDuplicateCodeReturns400() {
    // MWH.001 already exists in seed data
    String body = "{\"businessUnitCode\": \"MWH.001\", \"location\": \"AMSTERDAM-001\", \"capacity\": 10, \"stock\": 0}";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("warehouse")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(6)
  public void testCreateWarehouseWithInvalidLocationReturns400() {
    String body = "{\"businessUnitCode\": \"MWH.XYZ\", \"location\": \"INVALID-999\", \"capacity\": 10, \"stock\": 0}";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("warehouse")
        .then()
        .statusCode(400);
  }

  // -------------------------
  // POST /warehouse/{businessUnitCode}/replacement
  // -------------------------

  @Test
  @Order(7)
  public void testReplaceWarehouse() {
    String body = "{\"businessUnitCode\": \"MWH.012\", \"location\": \"AMSTERDAM-001\", \"capacity\": 50, \"stock\": 5}";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("warehouse/MWH.012/replacement")
        .then()
        .statusCode(200)
        .body(containsString("MWH.012"));
  }

  @Test
  @Order(8)
  public void testReplaceNonExistentWarehouseReturns404() {
    String body = "{\"businessUnitCode\": \"MWH.999\", \"location\": \"AMSTERDAM-001\", \"capacity\": 10, \"stock\": 0}";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("warehouse/MWH.999/replacement")
        .then()
        .statusCode(404);
  }

  // -------------------------
  // DELETE /warehouse/{id}
  // -------------------------

  @Test
  @Order(9)
  public void testSimpleCheckingArchivingWarehouses() {
    // List all — all 3 initial warehouses with locations present:
    given()
        .when()
        .get("warehouse")
        .then()
        .statusCode(200)
        .body(
            containsString("MWH.001"),
            containsString("ZWOLLE-001"),
            containsString("AMSTERDAM-001"),
            containsString("TILBURG-001"));

    // Archive warehouse with db id=1 (MWH.001, located at ZWOLLE-001):
    given().when().delete("warehouse/1").then().statusCode(204);

    // ZWOLLE-001 should be missing now (archived warehouses not listed):
    given()
        .when()
        .get("warehouse")
        .then()
        .statusCode(200)
        .body(
            not(containsString("ZWOLLE-001")),
            containsString("AMSTERDAM-001"),
            containsString("TILBURG-001"));
  }

  @Test
  @Order(10)
  public void testArchiveNonExistentWarehouseReturns404() {
    given()
        .when()
        .delete("warehouse/9999")
        .then()
        .statusCode(404);
  }
}
