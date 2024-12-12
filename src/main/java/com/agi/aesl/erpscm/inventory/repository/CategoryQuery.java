package com.agi.aesl.erpscm.inventory.repository;

public interface CategoryQuery {
    String getCategoriesWithSearch="""
            SELECT cat.id, cat.code, cat.name, cat.currentYearBudget, cat.productCount,
            cat.warehouse_id as warehouseId, cat.warehouseName as warehouseName, cat.storeName as storeName,
            (select count(*) from scm_item_categories ic3 
            LEFT JOIN scm_category_warehouse_stores cws2 ON cws2.category_id =ic3.id
            where ic3.parent_category_id = cat.id AND ic3.active=true
             AND ic3.category_status = 'APPROVED'
              AND (COALESCE(:warehouseId) IS NULL OR cws2.warehouse_id IN (:warehouseId))
             ) as subCategoryCount,
            cat.warehouse_store_id as warehouseStoreId
             FROM (
                       SELECT ic.id, ic.code, ic.name, cws.warehouse_id, cws.warehouse_store_id, 
                       sw.name as warehouseName, sws.store_name as storeName,
                       (COALESCE((
                               SELECT sum(amount) FROM scm_item_categories childCat
                               LEFT JOIN scm_category_budgets cb2 ON childCat.id = cb2.category_id
                           WHERE childCat.parent_category_id = ic.id
                           AND cb2.current_year = :year
                           ),0) ) as currentYearBudget,
                       ( select count(distinct item_id) from scm_item_stocks is2
                                 LEFT JOIN scm_items i on i.id=is2.item_id
                                 LEFT JOIN scm_category_warehouse_stores cws1 ON is2.warehouse_id  = cws1.warehouse_id
                                        AND is2.warehouse_store_id = cws1.warehouse_store_id
                                        AND i.item_parent_category_id = cws1.category_id
                              WHERE i.active = 1 AND  cws1.category_id=ic.id 
                              AND (COALESCE(:warehouseId) IS NULL OR is2.warehouse_id IN (:warehouseId))
                              AND (:warehouseStoreId IS NULL OR is2.warehouse_store_id = :warehouseStoreId)
                       ) as productCount
                            FROM scm_item_categories ic
                            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
                            LEFT JOIN scm_warehouse_stores sws ON sws.id = cws.warehouse_store_id
                            LEFT JOIN scm_warehouses sw ON sw.id = cws.warehouse_id
                            WHERE ic.active=1 AND ic.parent_category_id IS NULL 
                            AND (COALESCE(:warehouseId) IS NULL OR cws.warehouse_id IN (:warehouseId))
                            AND (:warehouseStoreId IS NULL OR cws.warehouse_store_id = :warehouseStoreId)
                            GROUP BY ic.id) cat 
                       WHERE (:name IS NULL OR cat.name LIKE concat(:name,'%'))
                        AND (:code IS NULL OR cat.code LIKE concat(:code,'%'))
                        AND (:currentYearBudget IS NULL OR cat.currentYearBudget LIKE concat(:currentYearBudget,'%')) 
                        AND (:productCount IS NULL OR cat.productCount=:productCount)
                        ORDER BY cat.id desc
             """;




    String countCategoriesWithSearch="SELECT COUNT(*) FROM ("+getCategoriesWithSearch+") as c";


    String getSubCategoriesWithSearch="""
            SELECT  
                cat.id, 
                cat.code, 
                cat.name, 
                cat.currentYearBudget, 
                cat.productCount, 
                cat.mainCategoryId, 
                cat.mainCategoryName, 
                cat.mainCategoryCode,
                cat.warehouse_id as warehouseId,
                cat.warehouse_store_id as warehouseStoreId,
                cat.storeName as storeName,
                cat.warehouseName as warehouseName
            FROM (
                SELECT
                    ic.id, 
                    ic.code,   
                    ic.name, 
                    ipc.id as mainCategoryId,
                    ipc.name as mainCategoryName, 
                    ipc.code as mainCategoryCode, 
                    sws.store_name as storeName,
                    sw.name as warehouseName,
                    cws.warehouse_id, 
                    cws.warehouse_store_id,
                    (sum(amount) + COALESCE((
                            SELECT sum(amount) FROM scm_item_categories childCat 
                            LEFT JOIN scm_category_budgets cb2 ON childCat.id = cb2.category_id 
                        WHERE childCat.parent_category_id = ic.id 
                        AND (:year IS NULL OR cb2.current_year = :year )
                        ),0) ) as currentYearBudget, 
                    ( select count(distinct item_id) from scm_item_stocks is2
                                 LEFT JOIN scm_items i on i.id=is2.item_id
                                 LEFT JOIN scm_category_warehouse_stores cws1 ON is2.warehouse_id  = cws1.warehouse_id
                                        AND is2.warehouse_store_id = cws1.warehouse_store_id
                                        AND i.item_category_id = cws1.category_id
                              WHERE i.active = 1 AND cws1.category_id=ic.id 
                              AND (COALESCE(:warehouseId) IS NULL OR is2.warehouse_id IN (:warehouseId))
                              AND (:warehouseStoreId IS NULL OR is2.warehouse_store_id = :warehouseStoreId)
                    ) as productCount
                FROM scm_item_categories ic 
                 LEFT JOIN scm_item_categories ipc ON ipc.id = ic.parent_category_id 
                 LEFT JOIN scm_category_budgets cb ON ic.id = cb.category_id 
                 LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
                 LEFT JOIN scm_warehouse_stores sws ON sws.id = cws.warehouse_store_id
                 LEFT JOIN scm_warehouses sw ON sw.id = cws.warehouse_id
                WHERE ic.active=1 AND ic.parent_category_id IS NOT NULL 
                AND (:year IS NULL OR cb.current_year = :year)
                 AND (COALESCE(:warehouseId) IS NULL OR cws.warehouse_id IN (:warehouseId))
                 AND (:warehouseStoreId IS NULL OR cws.warehouse_store_id = :warehouseStoreId)
                 GROUP BY ic.id) cat 
            WHERE (:name IS NULL OR cat.name LIKE concat(:name,'%')) 
             AND (:code IS NULL OR cat.code LIKE concat(:code,'%')) 
             AND (:currentYearBudget IS NULL OR cat.currentYearBudget LIKE concat(:currentYearBudget,'%')) 
             AND (:categoryId IS NULL OR cat.mainCategoryId =:categoryId) 
             AND (:productCount IS NULL OR cat.productCount=:productCount) 
            ORDER BY cat.id DESC
            """;

    String countSubCategoriesWithSearch="SELECT count(*) FROM (" + getSubCategoriesWithSearch +") p";

    String getMainCategoriesForInventoryControl= """
            SELECT ic.id as id, ic.name as name, ic.code as code,
            ic.active as active,GROUP_CONCAT(cws.warehouse_id) as warehouses,
            GROUP_CONCAT(w.name) as warehouseName,
            GROUP_CONCAT(ws.store_name) as storeName,
            ic.cps_category_id as cpsCategoryId,
            (SELECT COUNT(*) FROM scm_item_categories subCat 
            LEFT JOIN scm_category_warehouse_stores subCws ON subCws.category_id=subCat.id
            WHERE subCat.active=1 AND subCat.parent_category_id = ic.id
            AND (COALESCE(:warehouseId) IS NULL OR subCws.warehouse_id IN (:warehouseId))
            AND (:warehouseStoreId IS NULL OR subCws.warehouse_store_id = :warehouseStoreId)
            ) as subcategoryCount
            FROM scm_item_categories ic
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id=ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            LEFT JOIN scm_warehouses w ON w.id = cws.warehouse_id
            WHERE ic.parent_category_id IS NULL AND ic.active=true AND ic.cps_category_id IS NOT NULL
            AND ic.category_status IN ('APPROVED')
            AND (COALESCE(:warehouseId) IS NULL OR cws.warehouse_id IN (:warehouseId))
            AND (:warehouseStoreId IS NULL OR cws.warehouse_store_id = :warehouseStoreId)
            AND (:name IS NULL OR ic.name LIKE concat(:name,'%'))
            AND (:code IS NULL OR ic.code LIKE concat(:code,'%'))
            GROUP BY ic.id
            """;
    String countMainCategoriesForInventoryControl="SELECT COUNT(*) as total FROM ("+getMainCategoriesForInventoryControl+") as t";


    String getPendingMainCategories = """
            SELECT ic.id as id, ic.name as name, ic.code as code,
            ic.active as active,GROUP_CONCAT(cws.warehouse_id) as warehouses,
            ic.cps_category_id as cpsCategoryId,
            GROUP_CONCAT(w.name) as warehouseName,
            GROUP_CONCAT(ws.store_name) as storeName,
            (SELECT COUNT(*) FROM scm_item_categories subCat 
            LEFT JOIN scm_category_warehouse_stores subCws ON subCws.category_id=subCat.id
            WHERE subCat.active=0 AND subCat.parent_category_id = ic.id
            AND (:warehouseId IS NULL OR subCws.warehouse_id = :warehouseId)
            AND (:warehouseStoreId IS NULL OR subCws.warehouse_store_id = :warehouseStoreId)
            ) as subcategoryCount
            FROM scm_item_categories ic
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id=ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            LEFT JOIN scm_warehouses w ON w.id = cws.warehouse_id
            WHERE ic.parent_category_id IS NULL AND ic.active=false AND ic.category_status IN ('PENDING')
            AND ic.cps_category_id IS NOT NULL
            AND (COALESCE(:warehouseId) IS NULL OR cws.warehouse_id IN (:warehouseId))
            AND (:warehouseStoreId IS NULL OR cws.warehouse_store_id = :warehouseStoreId)
            AND (:name IS NULL OR ic.name LIKE concat(:name,'%'))
            AND (:code IS NULL OR ic.code LIKE concat(:code,'%'))
            GROUP BY ic.id
            """;

    String countPendingMainCategories="SELECT COUNT(*) as total FROM ("+getPendingMainCategories+") as t";

    String getSubCategoriesForInventoryControl="""
            SELECT ic.id as id, ic.name as name, ic.code as code,
            ipc.name as parentCategoryName, ipc.code as parentCategoryCode,
            ic.active as active,GROUP_CONCAT(cws.warehouse_id) as warehouses,
            GROUP_CONCAT(w.name) as warehouseName,
            GROUP_CONCAT(ws.store_name) as storeName
            FROM scm_item_categories ic
            LEFT JOIN scm_item_categories ipc ON ipc.id = ic.parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id=ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id=cws.warehouse_store_id
            LEFT JOIN scm_warehouses w ON w.id = cws.warehouse_id
            WHERE ic.parent_category_id IS NOT NULL AND ic.cps_category_id IS NOT NULL
            AND ic.active=true
            AND (COALESCE(:warehouseId) IS NULL OR cws.warehouse_id IN (:warehouseId))
            AND (:storeId IS NULL OR cws.warehouse_store_id = :storeId)
            AND (COALESCE(:parentCategoryId) IS NULL OR ic.parent_category_id IN (:parentCategoryId))
            AND (:name IS NULL OR ic.name LIKE concat(:name,'%'))
            AND (:code IS NULL OR ic.code LIKE concat(:code,'%'))
            GROUP BY ic.id
            """;
    String countSubCategoriesForInventoryControl="SELECT COUNT(*) FROM ("+getSubCategoriesForInventoryControl+") as t";

    String getPendingSubcategoriesForInventoryControl="""
            SELECT ic.id as id, ic.name as name, ic.code as code,
            ipc.name as parentCategoryName, ipc.code as parentCategoryCode,
            ic.active as active,
            GROUP_CONCAT(cws.warehouse_id) as warehouses,
            GROUP_CONCAT(w.name) as warehouseName,
            GROUP_CONCAT(ws.store_name) as storeName
            FROM scm_item_categories ic
            LEFT JOIN scm_item_categories ipc ON ipc.id = ic.parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id=ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id=cws.warehouse_store_id
            LEFT JOIN scm_warehouses w ON w.id = cws.warehouse_id
            WHERE ic.parent_category_id IS NOT NULL AND ic.cps_category_id IS NOT NULL
            AND ic.active=false
            AND (COALESCE(:warehouseId) IS NULL OR cws.warehouse_id IN (:warehouseId))
            AND (:storeId IS NULL OR cws.warehouse_store_id = :storeId)
            AND (:parentCategoryId IS NULL OR ic.parent_category_id = :parentCategoryId)
            AND (:name IS NULL OR ic.name LIKE concat(:name,'%'))
            AND (:code IS NULL OR ic.code LIKE concat(:code,'%'))
            GROUP BY ic.id
            """;
    String countPendingSubCategoriesForInventoryControl="SELECT COUNT(*) FROM ("+getPendingSubcategoriesForInventoryControl+") as t";

    String findAllSubCategories="""
            SELECT ic.id as id, ic.name as name, ic.code as code,
            ic.active as active,GROUP_CONCAT(cws.warehouse_id) as warehouses
            FROM scm_item_categories ic
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id=ic.id
            WHERE ic.active=1 AND ic.category_status IN ('APPROVED') AND ic.parent_category_id IS NOT NULL
                AND (:parentCategoryId IS NULL OR ic.parent_category_id = :parentCategoryId)
                AND (:name IS NULL OR ic.name LIKE concat(:name,'%'))
                AND (:code IS NULL OR ic.code LIKE concat(:code,'%'))
                AND (:storeId IS NULL OR cws.warehouse_store_id = :storeId)
           GROUP BY ic.id""";
}
