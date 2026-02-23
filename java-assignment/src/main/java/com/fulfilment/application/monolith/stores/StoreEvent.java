package com.fulfilment.application.monolith.stores;

public class StoreEvent {

  public enum Type {
    CREATED,
    UPDATED
  }

  public final Store store;
  public final Type type;

  public StoreEvent(Store store, Type type) {
    this.store = store;
    this.type = type;
  }
}
