package com.agi.aesl.erpscm.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.inventory.entity.ItemStock;

import java.util.List;

@Repository
public interface ItemStockRepository extends JpaRepository<ItemStock,Long>{

    List<ItemStock> findByWarehouseStoreId(Long id);

    @Query(value = """
            SELECT s FROM ItemStock s
            LEFT JOIN s.item i
            LEFT JOIN s.warehouse w
            WHERE i.id IN (:itemIds) AND w.id = :warehouseId
            """)
    List<ItemStock> findByItemsAndWarehosueId(List<Long> itemIds, Long warehouseId);
}
