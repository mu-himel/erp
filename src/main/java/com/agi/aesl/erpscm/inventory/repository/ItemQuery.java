package com.agi.aesl.erpscm.inventory.repository;

public interface ItemQuery {

    String getItemsWithSearch = """
            SELECT i.id as id, i.name as name, i.code as code, i.item_unit as itemUnit,
            ic.id as subCategoryId, ic.name as subCategoryName, ic.code as subCategoryCode,
            ipc.id as categoryId, ipc.name as categoryName, ipc.code as categoryCode,
            w.id as warehouseId, w.name as warehouseName,
            ws.id as warehouseStoreId, ws.store_name as warehouseStoreName,
            SUM(s.stock_qty) as qty,
            (SELECT sum(p.approved_quantity) FROM (SELECT
                        sdd.approved_quantity,
                        scb.name as brand_name,
                        sdd.item_id	
                       FROM scm_demand_details sdd
                       LEFT JOIN scm_demands sd ON sd.id = sdd.demand_id
                       LEFT JOIN scm_demand_detail_attributes sdda ON sdda.demand_detail_id = sdd.id
                       LEFT JOIN scm_category_brands scb ON scb.id = sdd.brand_id
                       WHERE sdd.item_id IS NOT NULL AND sd.warehouse_id = w.id
                        AND sdd.status IN ('PENDING_QC')  AND sdd.approved_quantity > 0
                        group by sdd.id
                       ) p WHERE p.item_id = i.id
                   GROUP BY p.brand_name, p.item_id) as inTransit,
            i.stock_threshold_qty as stockThresholdQty,
            i.reorder_percentage as reorderPercentage,
            i.item_attribute_name as itemAttributeName
            FROM scm_items i
            LEFT JOIN scm_item_stocks s ON s.item_id = i.id
            LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
            LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = s.warehouse_store_id
            LEFT JOIN scm_warehouses w ON w.id = s.warehouse_id
            LEFT JOIN scm_item_import_logs siil ON siil.item_id = i.id
            WHERE siil.warehouse_id IN (:warehouseId)
            AND siil.item_inactive_status IN ('APPROVED')
            AND i.active=1 AND (:name IS NULL OR i.name LIKE concat('%',:name,'%'))
            AND (:code IS NULL OR i.code LIKE concat('%',:code,'%'))
            AND ((:subCategoryId IS NULL OR ic.id = :subCategoryId)
            OR (COALESCE(:categoryId) IS NULL OR ic.id IN (:categoryId)))
            AND (COALESCE(:categoryId) IS NULL OR ipc.id IN (:categoryId))
            AND (:reorderPercentage IS NULL OR i.reorder_percentage = :reorderPercentage)
            AND (:stockThresholdQty IS NULL OR i.stock_threshold_qty = :stockThresholdQty)
            AND (COALESCE(:warehouseId) IS NULL OR w.id IN (:warehouseId))
            AND (:warehouseStoreId IS NULL OR ws.id = :warehouseStoreId)
            GROUP BY i.id ORDER BY i.name, i.item_attribute_name ASC""";

    String countItemsWithSearch = "SELECT count(*) FROM ("+getItemsWithSearch+") as p";

    String getItemInTransit= """
            SELECT sum(p.approved_quantity) FROM (SELECT
                        sdd.approved_quantity,
                        scb.name as brand_name,
                        sdd.item_id
                       FROM scm_demand_details sdd
                       LEFT JOIN scm_demands sd ON sd.id = sdd.demand_id
                       LEFT JOIN scm_demand_detail_attributes sdda ON sdda.demand_detail_id = sdd.id
                       LEFT JOIN scm_category_brands scb ON scb.id = sdd.brand_id
                       WHERE sdd.item_id IS NOT NULL AND sd.warehouse_id = :warehouseId
                        AND sdd.status IN ('PENDING_QC')  AND sdd.approved_quantity > 0
                        group by sdd.id
                       ) p WHERE p.item_id = :itemId
                   GROUP BY p.brand_name, p.item_id
            """;


    String getPendingItemsWithSearch = "SELECT i.id as id, i.name as name, i.code as code, " +
            "ic.id as subCategoryId, ic.name as subCategoryName, ic.code as subCategoryCode, " +
            "ipc.id as categoryId, ipc.name as categoryName, ipc.code as categoryCode," +
            "w.id as warehouseId, w.name as warehouseName, " +
            "ws.id as warehouseStoreId, ws.store_name as warehouseStoreName, " +
            "SUM(s.stock_qty) as qty," +
            " i.stock_threshold_qty as stockThresholdQty," +
            " i.reorder_percentage as reorderPercentage, " +
            " i.item_attribute_name as itemAttributeName " +
            "FROM scm_items i " +
            "LEFT JOIN scm_item_import_logs siil ON siil.item_id=i.id AND siil.warehouse_id=:warehouseId "+
            "LEFT JOIN scm_item_stocks s ON s.item_id = i.id " +
            "LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id " +
            "LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id " +
            "LEFT JOIN scm_warehouse_stores ws ON ws.id = s.warehouse_store_id " +
            "LEFT JOIN scm_warehouses w ON w.id = s.warehouse_id " +
            "WHERE i.active=0 " +
            "   AND siil.item_inactive_status IN ('PENDING') " +
            "   AND (:name IS NULL OR i.name LIKE concat(:name,'%')) " +
            "   AND (:code IS NULL OR i.code LIKE concat(:code,'%')) " +
            "   AND ((:subCategoryId IS NULL OR ic.id = :subCategoryId)" +
            "           OR (COALESCE(:categoryId) IS NULL OR ic.id IN (:categoryId))) " +
            "   AND (COALESCE(:categoryId) IS NULL OR ipc.id IN (:categoryId)) " +
            "   AND (:reorderPercentage IS NULL OR i.reorder_percentage = :reorderPercentage) " +
            "   AND (:stockThresholdQty IS NULL OR i.stock_threshold_qty = :stockThresholdQty) " +
            "   AND (:warehouseId IS NULL OR w.id = :warehouseId) " +
            "   AND (:warehouseStoreId IS NULL OR ws.id = :warehouseStoreId) " +
            "GROUP BY i.id";

    String countAllPendingItems = "SELECT COUNT(*) FROM ("+getPendingItemsWithSearch+") as total";


    String getPendingVerificationItemsWithSearch = """
                        SELECT i.id as id, i.name as name, i.code as code,
                        ic.id as subCategoryId, ic.name as subCategoryName, ic.code as subCategoryCode,
                        i.item_attribute_name as itemAttributeName,
                        ipc.id as categoryId, ipc.name as categoryName, ipc.code as categoryCode,
                        0 as qty,
                         i.stock_threshold_qty as stockThresholdQty,
                         i.reorder_percentage as reorderPercentage,
                         (SELECT MAX(la.account_status)
                                             FROM ledger_accounts la WHERE la.item_id = i.id ) as status,
                        w.name as warehouseName,
                        ws.store_name as warehouseStoreName
                        FROM scm_items i 
                        LEFT JOIN scm_item_import_logs siil ON siil.item_id = i.id AND siil.item_inactive_status = 'PENDING_VERIFICATION'
                        LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id 
                        LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id 
                        LEFT JOIN scm_item_stocks s ON s.item_id = i.id 
                        LEFT JOIN scm_warehouses w ON s.warehouse_id = w.id 
                        LEFT JOIN scm_warehouse_stores ws ON s.warehouse_store_id = ws.id 
                         WHERE i.active=0 
                        AND (s.warehouse_id IN (:warehouseId) AND siil.warehouse_id IN (:warehouseId))
                        AND (:warehouseStoreId IS NULL OR s.warehouse_store_id = :warehouseStoreId) 
                           AND (:name IS NULL OR i.name LIKE concat(:name,'%')) 
                           AND (:code IS NULL OR i.code LIKE concat(:code,'%')) 
                           AND ((:subCategoryId IS NULL OR ic.id = :subCategoryId) 
                                   OR (COALESCE(:categoryId) IS NULL OR ic.id IN (:categoryId)))
                           AND (COALESCE(:categoryId) IS NULL OR ipc.id IN (:categoryId)) 
                           AND (:reorderPercentage IS NULL OR i.reorder_percentage = :reorderPercentage) 
                           AND (:stockThresholdQty IS NULL OR i.stock_threshold_qty = :stockThresholdQty) 
                        GROUP BY i.id""";

    String countAllPendingVerificationItems = "SELECT COUNT(*) FROM ("+getPendingVerificationItemsWithSearch+") as total";

    String getItemsBySubCategoryAttributeAndName ="""
        SELECT p.id as id,
            p.name as name,
            p.code as code,
            p.brandId as brandId,
            p.brandName as brandName,
            p.warehouseId as warehouseId,
            p.warehouseStoreId as warehouseStoreId,
            p.active as active,
            p.attribute_types as attributeTypes,
            p.attribute_values as attributeValues,
            p.attribute_units as attributeUnits
            FROM (
            SELECT
                i.id as id,
                i.name as name,
                i.code as code,
                cb.id as brandId,
                cb.name as brandName,
                w.id as warehouseId,
                ws.id as warehouseStoreId,
                GROUP_CONCAT(DISTINCT ia.attribute_type,'_',ia.id ORDER BY ia.id) as attribute_types,
                GROUP_CONCAT(DISTINCT ia.attribute_value,'_',ia.id ORDER BY ia.id) as attribute_values,
                GROUP_CONCAT(DISTINCT ia.attribute_unit,'_',ia.id ORDER BY ia.id) as attribute_units,
                i.active
            FROM scm_items i
            LEFT JOIN scm_category_brands cb on cb.id = i.brand_id
            LEFT JOIN scm_item_attributes ia on ia.item_id = i.id
            LEFT JOIN scm_item_stocks is1 on is1.item_id = i.id
            LEFT JOIN scm_item_categories ic on ic.id = i.item_category_id
            LEFT JOIN scm_warehouses w on w.id = is1.warehouse_id
            LEFT JOIN scm_warehouse_stores ws on ws.id = is1.warehouse_store_id
            WHERE i.active=1
                AND (:brandId IS NULL OR cb.id = :brandId)
                AND (:name IS NULL OR i.name LIKE :name||'%')
                AND (:code IS NULL OR i.code LIKE :code||'%')
                AND (:subCategoryId IS NULL OR ic.id = :subCategoryId)
            GROUP BY w.id,i.id) p
            WHERE p.active = 1
            AND (:warehouseId IS NULL OR p.warehouseId = :warehouseId)
            AND (:attributeType IS NULL OR p.attribute_types LIKE CONCAT('%',:attributeType,'%'))
            AND (:attributeValue IS NULL OR p.attribute_values LIKE CONCAT('%',:attributeValue,'%'))
        """;
}
