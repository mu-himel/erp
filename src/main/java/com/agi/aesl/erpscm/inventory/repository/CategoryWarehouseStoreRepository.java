package com.agi.aesl.erpscm.inventory.repository;

import com.agi.aesl.erpscm.inventory.entity.CategoryWarehouseStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryWarehouseStoreRepository extends JpaRepository<CategoryWarehouseStore,Long> {
    void deleteByCategoryIdAndWarehouseIdAndWarehouseStoreId(Long id, Long warehouseId, Long storeId);

//    Optional<CategoryWarehouseStore> findByCategoryIdAndWarehouseStoreId(Long id, Long storeId);

    Boolean existsByWarehouseId(Long id);

    Optional<CategoryWarehouseStore> findByCategoryIdAndWarehouseId(Long id, Long id2);

    List<CategoryWarehouseStore> findByCategoryId(Long id);


    List<CategoryWarehouseStore> findByWarehouseStoreId(Long id);

    @Query(value = """
            SELECT COUNT(scws.id) from scm_item_categories sic
                        LEFT JOIN scm_category_warehouse_stores scws on sic.id = scws.category_id
                        WHERE sic.code LIKE CONCAT('%',:code,'%') AND scws.id IS NOT NULL
                        AND scws.warehouse_id = :warehouseId
            """,nativeQuery = true)
    int getCountCategoryCodeExistsInWarehouse(String code, Long warehouseId);
}
