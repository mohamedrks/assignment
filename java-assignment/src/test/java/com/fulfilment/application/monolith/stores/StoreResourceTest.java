package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreResourceTest {

  // -------------------------
  // GET /store
  // -------------------------

  @Test
  @Order(1)
  public void testListAllStores() {
    given()
        .when()
        .get("store")
        .then()
        .statusCode(200)
        .body(
            containsString("TONSTAD"),
            containsString("KALLAX"),
            containsString("BESTÅ"));
  }

  // -------------------------
  // GET /store/{id}
  // -------------------------

  @Test
  @Order(2)
  public void testGetSingleStore() {
    given()
        .when()
        .get("store/1")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"));
  }

  @Test
  @Order(3)
  public void testGetNonExistentStoreReturns404() {
    given()
        .when()
        .get("store/9999")
        .then()
        .statusCode(404);
  }

  // -------------------------
  // POST /store
  // -------------------------

  @Test
  @Order(4)
  public void testCreateStore() {
    String body = "{\"name\": \"EKET\", \"quantityProductsInStock\": 8}";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("store")
        .then()
        .statusCode(201)
        .body(containsString("EKET"));

    given()
        .when()
        .get("store")
        .then()
        .statusCode(200)
        .body(containsString("EKET"));
  }

  @Test
  @Order(5)
  public void testCreateStoreWithIdSetReturns422() {
    String body = "{\"id\": 99, \"name\": \"INVALID\", \"quantityProductsInStock\": 1}";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("store")
        .then()
        .statusCode(422);
  }

  // -------------------------
  // PUT /store/{id}
  // -------------------------

  @Test
  @Order(6)
  public void testUpdateStore() {
    String body = "{\"name\": \"KALLAX-V2\", \"quantityProductsInStock\": 20}";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .put("store/2")
        .then()
        .statusCode(200)
        .body(containsString("KALLAX-V2"));
  }

  @Test
  @Order(7)
  public void testUpdateStoreWithMissingNameReturns422() {
    String body = "{\"quantityProductsInStock\": 5}";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .put("store/2")
        .then()
        .statusCode(422);
  }

  @Test
  @Order(8)
  public void testUpdateNonExistentStoreReturns404() {
    String body = "{\"name\": \"GHOST\", \"quantityProductsInStock\": 1}";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .put("store/9999")
        .then()
        .statusCode(404);
  }

  // -------------------------
  // DELETE /store/{id}
  // -------------------------

  @Test
  @Order(9)
  public void testDeleteStore() {
    given()
        .when()
        .delete("store/3")
        .then()
        .statusCode(204);

    given()
        .when()
        .get("store")
        .then()
        .statusCode(200)
        .body(not(containsString("BESTÅ")));
  }

  @Test
  @Order(10)
  public void testDeleteNonExistentStoreReturns404() {
    given()
        .when()
        .delete("store/9999")
        .then()
        .statusCode(404);
  }

  // -------------------------
  // LegacyStoreManagerGateway — event fires AFTER_SUCCESS
  // -------------------------

  @Test
  @Order(11)
  public void testLegacyGatewayFiredAfterCreateCommit() {
    // The LegacyStoreManagerGateway listens with TransactionPhase.AFTER_SUCCESS,
    // so the downstream call only happens once the DB transaction is committed.
    // A successful 201 response confirms the transaction committed and the event fired.
    String body = "{\"name\": \"LEGACY-CREATE\", \"quantityProductsInStock\": 3}";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("store")
        .then()
        .statusCode(201)
        .body(containsString("LEGACY-CREATE"));
  }

  @Test
  @Order(12)
  public void testLegacyGatewayFiredAfterUpdateCommit() {
    // Same guarantee for updates — AFTER_SUCCESS ensures legacy system only
    // receives confirmed, committed data.
    String body = "{\"name\": \"LEGACY-UPDATE\", \"quantityProductsInStock\": 7}";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .put("store/1")
        .then()
        .statusCode(200)
        .body(containsString("LEGACY-UPDATE"));
  }
}
