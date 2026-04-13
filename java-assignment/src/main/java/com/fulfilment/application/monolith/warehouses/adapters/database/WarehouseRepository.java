package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return find("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = warehouse.businessUnitCode;
    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    db.createdAt = LocalDateTime.now();
    db.archivedAt = null;
    this.persist(db);
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse db = find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
        .firstResult();
    if (db == null) return;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    db.archivedAt = warehouse.archivedAt;
  }

  @Override
  public void remove(Warehouse warehouse) {
    DbWarehouse db = find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
        .firstResult();
    if (db != null) {
      db.archivedAt = LocalDateTime.now();
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse db = find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    return db != null ? db.toWarehouse() : null;
  }

  @Override
  public Warehouse findActiveById(Long id) {
    DbWarehouse db = findById(id);
    return (db != null && db.archivedAt == null) ? db.toWarehouse() : null;
  }

  public List<Warehouse> findActiveByLocation(String location) {
    return find("location = ?1 and archivedAt is null", location)
        .stream().map(DbWarehouse::toWarehouse).toList();
  }
}
