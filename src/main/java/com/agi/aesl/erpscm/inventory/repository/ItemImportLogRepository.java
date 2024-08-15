package com.agi.aesl.erpscm.inventory.repository;

import com.agi.aesl.erpscm.inventory.entity.ItemImportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemImportLogRepository extends JpaRepository<ItemImportLog,Long> {
    Optional<ItemImportLog> findByItemIdAndWarehouseId(Long itemId, Long warehouseId);
}
