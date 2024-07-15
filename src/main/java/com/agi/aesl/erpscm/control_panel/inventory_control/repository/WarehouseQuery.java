package com.agi.aesl.erpscm.control_panel.inventory_control.repository;

public interface WarehouseQuery {
    String getWarehouses="""
            SELECT 
            w.id as id, 
            w.name as name, 
            w.location as location,
            count(ws.id) as storeQty 
            FROM scm_warehouses w 
            LEFT JOIN scm_warehouse_stores ws ON ws.warehouse_id = w.id
                AND ws.active = true
            WHERE w.active = true
                AND (:name IS NULL OR w.name LIKE CONCAT(:name,'%'))
            GROUP BY w.id 
            """;

    String getWarehousesDetail="""
            SELECT 
            w
            FROM Warehouse w 
            WHERE w.active = true AND w.id=:id
            """;

    String countWarehouseWithPagination="""
            SELECT 
            count(*)
            FROM (SELECT 
            w.id as id, 
            w.name as name, 
            w.location as location,
            count(ws.id) as storeQty 
            FROM scm_warehouses w 
            LEFT JOIN scm_warehouse_stores ws ON ws.warehouse_id = w.id
                AND ws.active = true
            WHERE w.active = true 
                AND (:name IS NULL OR w.name LIKE CONCAT(:name,'%'))
            GROUP BY w.id ) total
            """;


    interface WarehouseInfo{
        Long getId();
        String getName();
        String getLocation();
        Integer getStoreQty();
    }
}
