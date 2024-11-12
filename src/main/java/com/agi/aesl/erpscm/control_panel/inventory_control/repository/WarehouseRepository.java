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
    @Query(value = getWarehouses, countQuery = countWarehouseWithPagination, nativeQuery = true)
    Page<WarehouseInfo> findAllByName(@Param("name") Optional<String> name, Pageable pageable);

    @Query(value = """
            SELECT w FROM Warehouse w
            WHERE w.name = :name
            """)
    Optional<Warehouse> findByName(String name);
    Optional<Warehouse> findByNameAndActive(String name,Boolean active);

    @Query(value = getWarehouses, nativeQuery = true)
    List<WarehouseInfo> findAllByName(Optional<String> name);

    @Query(value = getWarehousesDetail)
    Optional<Warehouse> findWarehouseById(Long id);

    @Query(value = getWarehousesWithFilter, countQuery = countWarehouseWithPaginationFilter, nativeQuery = true)
    Page<WarehouseInfo> findAllByNameAndId(List<Long> warehouseIds, String name, Pageable pageable);
}
