package com.fulfilment.application.monolith.stores;

public class StoreUpdatedEvent {
  public final Store store;

  public StoreUpdatedEvent(Store store) {
    this.store = store;
  }
}
