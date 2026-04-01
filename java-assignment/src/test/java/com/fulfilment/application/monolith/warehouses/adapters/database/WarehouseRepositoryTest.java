package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseRepositoryTest {

  @Inject WarehouseRepository warehouseRepository;

  // ── getAll returns only active ────────────────────────────────────────────

  @Test
  @Order(1)
  public void getAllShouldReturnOnlyActiveWarehouses() {
    List<Warehouse> all = warehouseRepository.getAll();
    // All returned rows must have null archivedAt
    assertTrue(all.stream().allMatch(w -> w.archivedAt == null));
    assertNotNull(all);
  }

  // ── findByBusinessUnitCode ────────────────────────────────────────────────

  @Test
  @Order(2)
  public void findByBusinessUnitCodeShouldReturnWarehouse() {
    Warehouse w = warehouseRepository.findByBusinessUnitCode("MWH.001");
    assertNotNull(w);
    assertEquals("MWH.001", w.businessUnitCode);
    assertEquals("ZWOLLE-001", w.location);
  }

  @Test
  @Order(3)
  public void findByBusinessUnitCodeShouldReturnNullWhenNotFound() {
    Warehouse w = warehouseRepository.findByBusinessUnitCode("DOES-NOT-EXIST");
    assertNull(w);
  }

  // ── findActiveByLocation ──────────────────────────────────────────────────

  @Test
  @Order(4)
  public void findActiveByLocationShouldReturnActiveWarehouses() {
    List<Warehouse> warehouses = warehouseRepository.findActiveByLocation("ZWOLLE-001");
    assertNotNull(warehouses);
    assertTrue(warehouses.stream().allMatch(w -> "ZWOLLE-001".equals(w.location)));
    assertTrue(warehouses.stream().allMatch(w -> w.archivedAt == null));
  }

  @Test
  @Order(5)
  public void findActiveByLocationShouldReturnEmptyForUnknownLocation() {
    List<Warehouse> warehouses = warehouseRepository.findActiveByLocation("NOWHERE-999");
    assertNotNull(warehouses);
    assertTrue(warehouses.isEmpty());
  }

  // ── create ────────────────────────────────────────────────────────────────

  @Test
  @Order(6)
  @Transactional
  public void createShouldPersistWarehouse() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "REPO-TEST-001";
    w.location = "HELMOND-001";   // HELMOND-001: max 1 warehouse, max capacity 45 — isolated
    w.capacity = 30;
    w.stock = 5;

    warehouseRepository.create(w);

    Warehouse found = warehouseRepository.findByBusinessUnitCode("REPO-TEST-001");
    assertNotNull(found);
    assertEquals("HELMOND-001", found.location);
    assertEquals(30, found.capacity);
  }

  // ── update ────────────────────────────────────────────────────────────────

  @Test
  @Order(7)
  @Transactional
  public void updateShouldModifyCapacityAndStock() {
    Warehouse w = warehouseRepository.findByBusinessUnitCode("MWH.012");
    assertNotNull(w);

    w.capacity = 60;
    w.stock = 10;
    warehouseRepository.update(w);

    Warehouse updated = warehouseRepository.findByBusinessUnitCode("MWH.012");
    assertEquals(60, updated.capacity);
    assertEquals(10, updated.stock);

    // Restore seed values so downstream @QuarkusTest classes see a consistent database
    updated.capacity = 50;
    updated.stock = 5;
    warehouseRepository.update(updated);
  }

  @Test
  @Order(8)
  @Transactional
  public void updateNonExistentWarehouseShouldNotThrow() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "NON-EXISTENT";
    w.capacity = 10;
    w.stock = 0;
    // Should silently do nothing (null guard in repository)
    warehouseRepository.update(w);
  }

  // ── remove ────────────────────────────────────────────────────────────────

  @Test
  @Order(9)
  @Transactional
  public void removeShouldSetArchivedAt() {
    // Create one to archive
    Warehouse w = new Warehouse();
    w.businessUnitCode = "REPO-ARCHIVE-001";
    w.location = "HELMOND-001";
    w.capacity = 20;
    w.stock = 0;
    warehouseRepository.create(w);

    Warehouse created = warehouseRepository.findByBusinessUnitCode("REPO-ARCHIVE-001");
    assertNotNull(created);

    warehouseRepository.remove(created);

    // After remove it should not be findable as active
    Warehouse archived = warehouseRepository.findByBusinessUnitCode("REPO-ARCHIVE-001");
    assertNull(archived);
  }

  @Test
  @Order(10)
  @Transactional
  public void removeNonExistentWarehouseShouldNotThrow() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "NON-EXISTENT";
    // Should silently do nothing (null guard in repository)
    warehouseRepository.remove(w);
  }
}
