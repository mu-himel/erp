package com.agi.aesl.erpscm.inventory.repository;

import com.agi.aesl.erpscm.inventory.entity.ItemImportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemImportLogRepository extends JpaRepository<ItemImportLog,Long> {
    Optional<ItemImportLog> findByItemIdAndWarehouseId(Long itemId, Long warehouseId);
    List<ItemImportLog> findByItemId(Long itemId);

    @Modifying
    @Query(value = """
            UPDATE scm_item_import_logs siil SET siil.item_inactive_status = 'APPROVED'
            WHERE siil.item_inactive_status IN ('PENDING','PENDING_VERIFICATION')
            """,nativeQuery = true)
    void forceActive();
}
