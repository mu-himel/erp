package com.agi.aesl.erpscm.control_panel.inventory_control.repository;

public interface WarehouseQuery {

    String COUNT_START="SELECT COUNT(*) FROM (";
    String COUNT_END=") AS TOTAL";
    String GET_WAREHOUSES="""
            SELECT
            w.id as id,
            w.name as name,
            w.location as location,
            count(ws.id) as storeQty
            FROM scm_warehouses w
            LEFT JOIN scm_warehouse_stores ws ON ws.warehouse_id = w.id
                AND ws.active = true
            WHERE w.active = true
                AND (:name IS NULL OR LOWER(w.name) LIKE CONCAT('%',LOWER(:name),'%'))
            GROUP BY w.id
            """;

    String GET_WAREHOUSE_LIST="""
            SELECT
            w.id as id,
            w.name as name,
            w.location as location,
            count(ws.id) as storeQty
            FROM scm_warehouses w
            LEFT JOIN scm_warehouse_stores ws ON ws.warehouse_id = w.id
                AND ws.active = true
            WHERE w.active = true
                AND (:name IS NULL OR LOWER(w.name) LIKE CONCAT('%',LOWER(:name),'%'))
                AND (COALESCE(:warehouseIds) IS NULL OR w.id IN (:warehouseIds))
            GROUP BY w.id
            """;

    String GET_WAREHOUSES_WITH_FILTER="""
            SELECT
            w.id as id,
            w.name as name,
            w.location as location,
            count(ws.id) as storeQty
            FROM scm_warehouses w
            LEFT JOIN scm_warehouse_stores ws ON ws.warehouse_id = w.id
                AND ws.active = true
            WHERE w.active = true
                AND (:name IS NULL OR LOWER(w.name) LIKE CONCAT('%',LOWER(:name),'%'))
                AND w.id IN (:warehouseIds)
            GROUP BY w.id
            """;

    String GET_WAREHOUSE_DETAIL="""
            SELECT
            w
            FROM Warehouse w
            WHERE w.active = true AND w.id=:id
            """;

    String COUNT_WAREHOUSE_WITH_PAGINATION=COUNT_START+GET_WAREHOUSES+COUNT_END;

    String COUNT_WAREHOUSE_WITH_PAGINATION_FILTER=COUNT_START+GET_WAREHOUSES_WITH_FILTER+COUNT_END;

    interface WarehouseInfo{
        Long getId();
        String getName();
        String getLocation();
        Integer getStoreQty();
    }

    String GET_FINISH_GOODS_WAREHOUSES="""
        SELECT sw.id, name,sws.id as storeId,
            (SELECT COUNT(*) FROM scm_category_warehouse_stores scws
                    LEFT JOIN scm_item_categories sic ON sic.id = scws.category_id
                    WHERE scws.warehouse_store_id = sws.id
                    AND sic.parent_category_id IS NULL
                    AND sic.active=1
            )  as categoriesCount from scm_warehouses sw
            LEFT JOIN scm_warehouse_stores sws ON sws.warehouse_id =sw.id
            WHERE
            (:name IS NULL OR LOWER(sw.name) LIKE CONCAT('%',LOWER(:name),'%'))
            AND LOWER(sws.store_name) LIKE CONCAT('%','finish','%') AND sws.active=true
        """;

    String COUNT_FINISH_GOODS_WAREHOUSES = COUNT_START+GET_FINISH_GOODS_WAREHOUSES+COUNT_END;

    String GET_FINISH_GOODS_WAREHOUSES_FOR_IDS="""
        SELECT sw.id  from scm_warehouses sw
        LEFT JOIN scm_warehouse_stores sws ON sws.warehouse_id =sw.id
        WHERE
        (:name IS NULL OR LOWER(sw.name) LIKE CONCAT('%',LOWER(:name),'%'))
        AND LOWER(sws.store_name) LIKE CONCAT('%','finish','%') AND sws.active=true
    """;
}
