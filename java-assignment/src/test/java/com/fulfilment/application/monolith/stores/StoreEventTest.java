package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StoreEventTest {

  @Test
  void shouldStoreCreatedTypeAndStore() {
    Store store = new Store("TONSTAD");
    StoreEvent event = new StoreEvent(store, StoreEvent.Type.CREATED);

    assertSame(store, event.store);
    assertEquals(StoreEvent.Type.CREATED, event.type);
  }

  @Test
  void shouldStoreUpdatedTypeAndStore() {
    Store store = new Store("KALLAX");
    StoreEvent event = new StoreEvent(store, StoreEvent.Type.UPDATED);

    assertSame(store, event.store);
    assertEquals(StoreEvent.Type.UPDATED, event.type);
  }

  @Test
  void enumShouldHaveTwoValues() {
    StoreEvent.Type[] values = StoreEvent.Type.values();
    assertEquals(2, values.length);
    assertEquals(StoreEvent.Type.CREATED, StoreEvent.Type.valueOf("CREATED"));
    assertEquals(StoreEvent.Type.UPDATED, StoreEvent.Type.valueOf("UPDATED"));
  }
}
