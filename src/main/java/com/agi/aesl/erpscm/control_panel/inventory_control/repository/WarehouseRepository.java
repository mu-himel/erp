package com.agi.aesl.erpscm.control_panel.inventory_control.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse,Long>, WarehouseQuery {

    @Query(value = GET_WAREHOUSES, countQuery = COUNT_WAREHOUSE_WITH_PAGINATION, nativeQuery = true)
    Page<WarehouseInfo> findAllByName(@Param("name") Optional<String> name, Pageable pageable);

    @Query(value = """
            SELECT w FROM Warehouse w
            WHERE w.name = :name
            """)
    Optional<Warehouse> findByName(String name);
    Optional<Warehouse> findByNameAndActive(String name,Boolean active);

    @Query(value = GET_WAREHOUSES, nativeQuery = true)
    List<WarehouseInfo> findAllByName(Optional<String> name);

    @Query(value = GET_WAREHOUSE_LIST, nativeQuery = true)
    List<WarehouseInfo> findAllByIdsAndName(List<Long> warehouseIds, Optional<String> name);

    @Query(value = GET_WAREHOUSE_DETAIL)
    Optional<Warehouse> findWarehouseById(Long id);

    @Query(value = GET_WAREHOUSES_WITH_FILTER, countQuery = COUNT_WAREHOUSE_WITH_PAGINATION_FILTER, nativeQuery = true)
    Page<WarehouseInfo> findAllByNameAndId(List<Long> warehouseIds, String name, Pageable pageable);

    @Query(value = GET_FINISH_GOODS_WAREHOUSES, countQuery=COUNT_FINISH_GOODS_WAREHOUSES,nativeQuery = true)
    Page<WarehouseInfoExt> findOnlyFinishedGoodsWarehouse(String name, Pageable pageable);

    @Query(value = GET_FINISH_GOODS_WAREHOUSES_FOR_IDS,nativeQuery = true)
    List<Long> findOnlyFinishedGoodsWarehouse(String name);
    public interface WarehouseInfoExt {
    
        Long getId();
        String getName();
        Long getStoreId();
        Long getCategoriesCount();
    }
}
