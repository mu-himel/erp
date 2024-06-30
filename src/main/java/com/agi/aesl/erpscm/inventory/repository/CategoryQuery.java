package com.agi.aesl.erpscm.inventory.repository;

public interface CategoryQuery {
    String getCategoriesWithSearch="""
            SELECT cat.id, cat.code, cat.name, cat.currentYearBudget, cat.productCount,
            cat.warehouse_id as warehouseId,
            cat.warehouse_store_id as warehouseStoreId
             FROM (
                       SELECT ic.id, ic.code, ic.name, cws.warehouse_id, cws.warehouse_store_id, 
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
                              AND (:warehouseId IS NULL OR is2.warehouse_id = :warehouseId)
                              AND (:warehouseStoreId IS NULL OR is2.warehouse_store_id = :warehouseStoreId)
                       ) as productCount
                            FROM scm_item_categories ic
                            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
                            WHERE ic.active=1 AND ic.parent_category_id IS NULL 
                            AND (:warehouseId IS NULL OR cws.warehouse_id = :warehouseId)
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
                cat.warehouse_store_id as warehouseStoreId
            FROM (
                SELECT
                    ic.id, 
                    ic.code,   
                    ic.name, 
                    ipc.id as mainCategoryId,
                    ipc.name as mainCategoryName, 
                    ipc.code as mainCategoryCode, 
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
                              AND (:warehouseId IS NULL OR is2.warehouse_id = :warehouseId)
                              AND (:warehouseStoreId IS NULL OR is2.warehouse_store_id = :warehouseStoreId)
                    ) as productCount
                FROM scm_item_categories ic 
                 LEFT JOIN scm_item_categories ipc ON ipc.id = ic.parent_category_id 
                 LEFT JOIN scm_category_budgets cb ON ic.id = cb.category_id 
                 LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
                WHERE ic.active=1 AND ic.parent_category_id IS NOT NULL 
                AND (:year IS NULL OR cb.current_year = :year)
                 AND (:warehouseId IS NULL OR cws.warehouse_id = :warehouseId)
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

}
