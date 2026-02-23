package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LegacyStoreManagerGatewayTest {

  private final LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();

  @Test
  void onStoreEventShouldNotThrowForValidStore() {
    Store store = new Store("TONSTAD");
    store.quantityProductsInStock = 5;
    StoreEvent event = new StoreEvent(store, StoreEvent.Type.CREATED);

    assertDoesNotThrow(() -> gateway.onStoreEvent(event));
  }

  @Test
  void onStoreEventShouldNotThrowForUpdatedEvent() {
    Store store = new Store("KALLAX");
    store.quantityProductsInStock = 10;
    StoreEvent event = new StoreEvent(store, StoreEvent.Type.UPDATED);

    assertDoesNotThrow(() -> gateway.onStoreEvent(event));
  }

  @Test
  void onStoreEventShouldNotThrowWhenStockIsZero() {
    Store store = new Store("BESTÅ");
    store.quantityProductsInStock = 0;
    StoreEvent event = new StoreEvent(store, StoreEvent.Type.CREATED);

    assertDoesNotThrow(() -> gateway.onStoreEvent(event));
  }
}
