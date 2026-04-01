package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class StoreEventHandler {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  /**
   * This method listens for StoreEvent ONLY after the transaction has been
   * successfully committed to the database (AFTER_SUCCESS).
   *
   * This guarantees the legacy system receives confirmed data only.
   */
  public void onStoreEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreEvent event) {
    if (event.getAction() == StoreEvent.Action.CREATE) {
      legacyStoreManagerGateway.createStoreOnLegacySystem(event.getStore());
    } else if (event.getAction() == StoreEvent.Action.UPDATE) {
      legacyStoreManagerGateway.updateStoreOnLegacySystem(event.getStore());
    }
  }
}
