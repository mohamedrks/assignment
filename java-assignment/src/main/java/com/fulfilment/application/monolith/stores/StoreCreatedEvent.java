package com.fulfilment.application.monolith.stores;

public class StoreCreatedEvent {
  public final Store store;

  public StoreCreatedEvent(Store store) {
    this.store = store;
  }
}
