package com.agi.aesl.erpscm.control_panel.inventory_control.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;

@Repository
public interface WarehouseStoreRepository extends JpaRepository<WarehouseStore,Long>{

    @Query(value = """
            SELECT 
            ws.id as id,
            ws.active as active,
            ws.store_name as storeName,
            w.name as warehouseName,
            (SELECT COUNT(*) FROM scm_category_warehouse_stores scws 
            LEFT JOIN scm_item_categories sic ON sic.id = scws.category_id
            WHERE scws.warehouse_store_id = ws.id
            AND sic.parent_category_id IS NULL
            AND sic.active=1
            ) as categoriesCount
            FROM scm_warehouse_stores ws
            LEFT JOIN scm_warehouses w ON w.id = ws.warehouse_id
            WHERE ws.active=:active
            AND ws.warehouse_id = :warehouseId
            """,nativeQuery = true)
    Page<WarehouseStoreInfoV2> findAllByActiveAndWarehouseId(Boolean active, Long warehouseId, Pageable pageable);

    interface WarehouseStoreInfoV2{
        Long getId();
        Boolean getActive();
        String getStoreName();
        String getWarehouseName();
        Long getCategoriesCount();
    }

    @Query(value = """
                SELECT 
                    ws.id as id,
                    ws.warehouse_id as warehouseId,
                    w.name as warehouseName,
                    ws.store_id as storeId,
                    ws.store_name as storeName,
                    ws.alias as alias,
                    ws.active as active ,
                    (SELECT count(*) FROM scm_category_warehouse_stores cws
                            LEFT JOIN scm_item_categories cat ON cat.id = cws.category_id
                             WHERE cat.active=1 AND cat.parent_category_id IS NULL AND cws.warehouse_id = ws.warehouse_id 
                             AND cws.warehouse_store_id = ws.id) as categoriesCount
                FROM scm_warehouse_stores ws
                LEFT JOIN scm_warehouses w ON w.id = ws.warehouse_id
                WHERE ws.active =1 AND ( COALESCE(:warehouseId) IS NULL OR ws.warehouse_id IN (:warehouseId))
            """,nativeQuery = true)
    List<WarehouseStoreInfo> findAllByWarehouseId(List<Long> warehouseId);
    List<WarehouseStore> findAllByWarehouseId(Long warehouseId);


    @Query(value = "SELECT ws FROM WarehouseStore ws WHERE ws.id=:id")
    Optional<WarehouseStoreInfoSingle> findStoreById(Long id);

    @Query(value = "SELECT ws FROM WarehouseStore ws WHERE ws.warehouse.id=:wId and LOWER(ws.storeName) = LOWER(:storeName) ")
    Optional<WarehouseStoreInfoSingle> findByWarehouseIdAndStoreName(
        @Param("wId") Long wId,
        @Param("storeName") String storeName
    );

    @Query(value = "SELECT ws FROM WarehouseStore ws WHERE ws.id=:id AND ws.warehouse.id=:wId")
    Optional<WarehouseStoreInfoSingle> findByIdAndWarehouseId(
            @Param("id") Long id,
            @Param("wId") Long wId
    );

    Boolean existsByWarehouseId(Long id);

    Optional<WarehouseStore> findByStoreNameAndWarehouseIdAndActive(String name,Long warehouseId,Boolean active);

    interface WarehouseStoreInfoSingle{
        Long getId();
        String getStoreName();

        WarehouseInfo getWarehouse();
    }

    interface WarehouseStoreInfo{
        Long getId();
        Long getStoreId();
        String getStoreName();
        String getAlias();

        Boolean getActive();

        Long getWarehouseId();
        String getWarehouseName();

        Long getCategoriesCount();
    }

    interface WarehouseInfo{
        Long getId();
        String getName();
        String getLocation();
    }

    Boolean existsByWarehouseIdAndActive(Long id, Boolean active);
}
