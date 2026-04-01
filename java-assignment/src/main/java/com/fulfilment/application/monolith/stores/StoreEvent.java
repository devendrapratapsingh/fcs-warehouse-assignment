package com.fulfilment.application.monolith.stores;

// A simple wrapper to carry the Store and the action type (CREATE or UPDATE)
public class StoreEvent {

  public enum Action {
    CREATE,
    UPDATE
  }

  private final Store store;
  private final Action action;

  public StoreEvent(Store store, Action action) {
    this.store = store;
    this.action = action;
  }

  public Store getStore() {
    return store;
  }

  public Action getAction() {
    return action;
  }
}
