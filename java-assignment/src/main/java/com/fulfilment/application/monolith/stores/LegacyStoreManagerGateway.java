package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LegacyStoreManagerGateway {

  private static final Logger LOGGER = Logger.getLogger(LegacyStoreManagerGateway.class);

  public void createStoreOnLegacySystem(Store store) {
    writeToFile(store);
  }

  public void updateStoreOnLegacySystem(Store store) {
    writeToFile(store);
  }

  private void writeToFile(Store store) {
    try {
      Path tempFile = Files.createTempFile(store.name, ".txt");
      LOGGER.debugf("Temporary file created at: %s", tempFile);

      String content = "Store created. [ name =" + store.name
          + " ] [ items on stock =" + store.quantityProductsInStock + "]";
      Files.write(tempFile, content.getBytes());
      LOGGER.debugf("Data written to temporary file for store: %s", store.name);

      String readContent = new String(Files.readAllBytes(tempFile));
      LOGGER.debugf("Verified file content: %s", readContent);

      Files.delete(tempFile);
      LOGGER.debugf("Temporary file deleted for store: %s", store.name);

    } catch (Exception e) {
      LOGGER.errorf(e, "Failed to write legacy sync file for store: %s", store.name);
    }
  }
}
