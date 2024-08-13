package com.agi.aesl.erpscm.inventory.repository;

public interface ItemQuery {

    String getItemsWithSearch = "SELECT i.id as id, i.name as name, i.code as code, " +
            "ic.id as subCategoryId, ic.name as subCategoryName, ic.code as subCategoryCode, " +
            "ipc.id as categoryId, ipc.name as categoryName, ipc.code as categoryCode," +
            "w.id as warehouseId, w.name as warehouseName, " +
            "ws.id as warehouseStoreId, ws.store_name as warehouseStoreName, " +
            "SUM(s.stock_qty) as qty," +
            " i.stock_threshold_qty as stockThresholdQty," +
            " i.reorder_percentage as reorderPercentage " +
            "FROM scm_items i " +
            "LEFT JOIN scm_item_stocks s ON s.item_id = i.id " +
            "LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id " +
            "LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id " +
            "LEFT JOIN scm_warehouse_stores ws ON ws.id = s.warehouse_store_id " +
            "LEFT JOIN scm_warehouses w ON w.id = s.warehouse_id " +
            "WHERE i.active=1 AND (:name IS NULL OR i.name LIKE concat(:name,'%')) " +
            "   AND (:code IS NULL OR i.code LIKE concat(:code,'%')) " +
            "   AND (:subCategoryId IS NULL OR ic.id = :subCategoryId) " +
            "   AND (:categoryId IS NULL OR ipc.id = :categoryId) " +
            "   AND (:reorderPercentage IS NULL OR i.reorder_percentage = :reorderPercentage) " +
            "   AND (:stockThresholdQty IS NULL OR i.stock_threshold_qty = :stockThresholdQty) " +
            "   AND (:warehouseId IS NULL OR w.id = :warehouseId) " +
            "   AND (:warehouseStoreId IS NULL OR ws.id = :warehouseStoreId) " +
            "GROUP BY i.id";

    String countItemsWithSearch = "SELECT count(*) FROM (" +
            "SELECT i.id as id, i.name as name, i.code as code, " +
            "ic.id as subCategoryId, ic.name as subCategoryName, ic.code as subCategoryCode, " +
            "ipc.id as categoryId, ipc.name as categoryName, ipc.code as categoryCode," +
            "w.id as warehouseId, w.name as warehouseName, " +
            "ws.id as warehouseStoreId, ws.store_name as warehouseStoreName, " +
            "SUM(s.stock_qty) as qty," +
            " i.stock_threshold_qty as stockThresholdQty," +
            " i.reorder_percentage as reorderPercentage " +
            "FROM scm_items i " +
            "LEFT JOIN scm_item_stocks s ON s.item_id = i.id " +
            "LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id " +
            "LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id " +
            "LEFT JOIN scm_warehouse_stores ws ON ws.id = s.warehouse_store_id " +
            "LEFT JOIN scm_warehouses w ON w.id = s.warehouse_id " +
            "WHERE i.active=1 AND (:name IS NULL OR i.name LIKE concat(:name,'%')) " +
            "   AND (:code IS NULL OR i.code LIKE concat(:code,'%')) " +
            "   AND (:subCategoryId IS NULL OR ic.id = :subCategoryId) " +
            "   AND (:categoryId IS NULL OR ipc.id = :categoryId) " +
            "   AND (:reorderPercentage IS NULL OR i.reorder_percentage = :reorderPercentage) " +
            "   AND (:stockThresholdQty IS NULL OR i.stock_threshold_qty = :stockThresholdQty) " +
            "   AND (:warehouseId IS NULL OR w.id = :warehouseId) " +
            "   AND (:warehouseStoreId IS NULL OR ws.id = :warehouseStoreId) " +
            "GROUP BY i.id) p";


    String getPendingItemsWithSearch = "SELECT i.id as id, i.name as name, i.code as code, " +
            "ic.id as subCategoryId, ic.name as subCategoryName, ic.code as subCategoryCode, " +
            "ipc.id as categoryId, ipc.name as categoryName, ipc.code as categoryCode," +
            "w.id as warehouseId, w.name as warehouseName, " +
            "ws.id as warehouseStoreId, ws.store_name as warehouseStoreName, " +
            "SUM(s.stock_qty) as qty," +
            " i.stock_threshold_qty as stockThresholdQty," +
            " i.reorder_percentage as reorderPercentage " +
            "FROM scm_items i " +
            "LEFT JOIN scm_item_stocks s ON s.item_id = i.id " +
            "LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id " +
            "LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id " +
            "LEFT JOIN scm_warehouse_stores ws ON ws.id = s.warehouse_store_id " +
            "LEFT JOIN scm_warehouses w ON w.id = s.warehouse_id " +
            "WHERE i.active=0 " +
            "   AND i.item_inactive_status IN ('PENDING') " +
            "   AND (:name IS NULL OR i.name LIKE concat(:name,'%')) " +
            "   AND (:code IS NULL OR i.code LIKE concat(:code,'%')) " +
            "   AND (:subCategoryId IS NULL OR ic.id = :subCategoryId) " +
            "   AND (:categoryId IS NULL OR ipc.id = :categoryId) " +
            "   AND (:reorderPercentage IS NULL OR i.reorder_percentage = :reorderPercentage) " +
            "   AND (:stockThresholdQty IS NULL OR i.stock_threshold_qty = :stockThresholdQty) " +
            "   AND (:warehouseId IS NULL OR w.id = :warehouseId) " +
            "   AND (:warehouseStoreId IS NULL OR ws.id = :warehouseStoreId) " +
            "GROUP BY i.id";

    String countAllPendingItems = "SELECT COUNT(*) FROM ("+getPendingItemsWithSearch+") as total";


    String getPendingVerificationItemsWithSearch = "SELECT i.id as id, i.name as name, i.code as code, " +
            "ic.id as subCategoryId, ic.name as subCategoryName, ic.code as subCategoryCode, " +
            "ipc.id as categoryId, ipc.name as categoryName, ipc.code as categoryCode," +
//            "w.id as warehouseId, w.name as warehouseName, " +
//            "ws.id as warehouseStoreId, ws.store_name as warehouseStoreName, " +
            "0 as qty," +
            " i.stock_threshold_qty as stockThresholdQty," +
            " i.reorder_percentage as reorderPercentage " +
            "FROM scm_items i " +
            "LEFT JOIN scm_item_import_logs siil ON siil.item_id = i.id AND siil.item_inactive_status = 'PENDING_VERIFICATION'"+
            "LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id " +
            "LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id " +
            "LEFT JOIN scm_item_stocks s ON s.item_id = i.id " +
            " WHERE i.active=0 " +
            "AND s.warehouse_id = :warehouseId "+
            "AND s.warehouse_store_id = :warehouseStoreId "+
//            "   AND i.item_inactive_status IN ('PENDING_VERIFICATION') " +
            "   AND (:name IS NULL OR i.name LIKE concat(:name,'%')) " +
            "   AND (:code IS NULL OR i.code LIKE concat(:code,'%')) " +
            "   AND (:subCategoryId IS NULL OR ic.id = :subCategoryId) " +
            "   AND (:categoryId IS NULL OR ipc.id = :categoryId) " +
            "   AND (:reorderPercentage IS NULL OR i.reorder_percentage = :reorderPercentage) " +
            "   AND (:stockThresholdQty IS NULL OR i.stock_threshold_qty = :stockThresholdQty) " +
            "GROUP BY i.id";

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
            GROUP BY i.id) p
            WHERE p.active = 1
            AND (:warehouseId IS NULL OR p.warehouseId = :warehouseId)
            AND (:attributeType IS NULL OR p.attribute_types LIKE CONCAT('%',:attributeType,'%'))
            AND (:attributeValue IS NULL OR p.attribute_values LIKE CONCAT('%',:attributeValue,'%'))
        """;
}
