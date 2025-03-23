package com.agi.aesl.erpscm.inventory.user_request.repository;

public class UserCategoryQuery {
    private UserCategoryQuery(){}
    public static final String COUNT_START="SELECT COUNT(*) FROM (";
    public static final String COUNT_END=") AS TOTAL";
    public static final String GET_MY_CATEGORIES = """
            select id as id, name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.parent_category_id=uc.id
            ) as subCategoryCount, 0 as productCount,
            (select count(id) from scm_item_categories ic WHERE ic.active=1 AND ic.user_category_id = uc.id) as active, 
            category_status as status
            FROM user_categories uc
            WHERE uc.created_by_id=:userId 
            AND uc.category_status NOT IN ('VERIFIED','APPROVED','COMPLETED','REJECTED','MERGED')
            AND (uc.parent_category_id IS NULL AND uc.active_parent_category_id IS NULL)
            """;
    public static final String COUNT_MY_CATEGORIES=COUNT_START+ GET_MY_CATEGORIES+COUNT_END;


    public static final String GET_MY_SUB_CATEGORIES = """
            select uc.id as id, 
            (select count(id) from scm_item_categories ic WHERE ic.active=1 AND ic.user_category_id = uc.id) as active, 
            puc.name as categoryName, uc.name as subCategoryName, 0 as productCount,
            uc.category_status as status
             FROM user_categories uc
             LEFT JOIN scm_item_categories puc ON (puc.id = uc.parent_category_id OR puc.id = uc.active_parent_category_id )
             WHERE uc.created_by_id=:userId 
             AND uc.category_status NOT IN ('VERIFIED','APPROVED','COMPLETED','REJECTED','MERGED')
             AND (uc.active_parent_category_id IS NOT NULL OR uc.parent_category_id IS NOT NULL)
             AND (:categoryId IS NULL OR uc.parent_category_id = :categoryId OR uc.active_parent_category_id = :categoryId)
            """;
    public static final String COUNT_MY_SUB_CATEGORIES = COUNT_START+GET_MY_SUB_CATEGORIES+COUNT_END;

    public static final String GET_CATEGORY_PVS = """
            select uc.id as id, name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.parent_category_id=uc.id
            ) as subCategoryCount, 0 as productCount,
            CASE WHEN uc.category_status != 'REVIEW' AND (uch.id IS NOT NULL AND uch.employee_id = :userId) THEN
                    uch.category_status
                ELSE
                    uc.category_status
            END  as status,
            (select count(id) from scm_item_categories ic WHERE ic.active=1 AND ic.user_category_id = uc.id) as active
            FROM user_categories uc
            LEFT JOIN user_category_histories uch ON uch.user_category_id = uc.id
            WHERE (
                (uc.next_verifier_id=:userId AND uc.category_status IN ('PENDING_VERIFICATION','REVIEW','VERIFIED'))
                OR 
                (uch.employee_id = :userId AND uch.category_status = 'VERIFIED')
            )
            AND uc.parent_category_id IS NULL
            """;
    public static final String COUNT_CATEGORY_PVS = COUNT_START+GET_CATEGORY_PVS+COUNT_END;

    public static final String GET_SUB_CATEGORY_PVS="""
            select uc.id as id, puc.name as categoryName, uc.name as subCategoryName, 0 as productCount,
            CASE WHEN uc.category_status != 'REVIEW' AND (uch.id IS NOT NULL AND uch.employee_id = :userId) THEN
                    uch.category_status
                ELSE
                    uc.category_status
            END  as status,
            (select count(id) from scm_item_categories ic WHERE ic.active=1 AND ic.user_category_id = uc.id) as active
             FROM user_categories uc
             LEFT JOIN user_category_histories uch ON uch.user_category_id = uc.id
             LEFT JOIN user_categories puc ON puc.id = uc.parent_category_id
             WHERE (
                (uc.next_verifier_id=:userId AND uc.category_status IN ('PENDING_VERIFICATION','REVIEW','VERIFIED'))
                OR 
                (uch.employee_id = :userId AND uch.category_status = 'VERIFIED' )
             )
             AND (:categoryId IS NULL OR uc.parent_category_id = :categoryId)
            """;
    public static final String COUNT_SUB_CATEGORY_PVS= COUNT_START+GET_SUB_CATEGORY_PVS+COUNT_END;

    public static final String GET_CATEGORY_PAS="""
            select uc.id as id, name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.parent_category_id=uc.id
            ) as subCategoryCount, 0 as productCount,
            CASE WHEN uc.category_status != 'REVIEW' AND (uch.id IS NOT NULL AND uch.employee_id = :userId) THEN
                    uch.category_status
                ELSE
                    uc.category_status
            END  as status,
            (select count(id) from scm_item_categories ic WHERE ic.active=1 AND ic.user_category_id = uc.id) as active
            FROM user_categories uc
            LEFT JOIN user_category_histories uch ON uch.user_category_id = uc.id
            WHERE (
                (uc.next_approver_id=:userId  AND uc.category_status IN ('PENDING_APPROVAL','REVIEW','APPROVED'))
                OR 
                (uch.employee_id = :userId AND uch.category_status = 'APPROVED' )
            )
            AND uc.parent_category_id IS NULL
            """;
    public static final String COUNT_CATEGORY_PAS = COUNT_START+GET_CATEGORY_PAS+COUNT_END;

    public static final String GET_SUB_CATEGORY_PAS="""
            select uc.id as id, puc.name as categoryName, uc.name as subCategoryName, 0 as productCount,
            CASE WHEN uc.category_status != 'REVIEW' AND (uch.id IS NOT NULL AND uch.employee_id = :userId) THEN
                    uch.category_status
                ELSE
                    uc.category_status
            END  as status,
            (select count(id) from scm_item_categories ic WHERE ic.active=1 AND ic.user_category_id = uc.id) as active
            FROM user_categories uc
            LEFT JOIN user_category_histories uch ON uch.user_category_id = uc.id
            LEFT JOIN user_categories puc ON puc.id = uc.parent_category_id
            WHERE (
                (uc.next_approver_id=:userId AND uc.category_status IN ('PENDING_APPROVAL','REVIEW','APPROVED'))
                OR 
                (uch.employee_id = :userId AND uch.category_status = 'APPROVED')
            )
            AND (:categoryId IS NULL OR uc.parent_category_id = :categoryId)
            """;
    public static final String COUNT_SUB_CATEGORY_PAS = COUNT_START+GET_SUB_CATEGORY_PAS+COUNT_END;

    public static final String CLOSED_CATEGORIES="""
            select uc.id as id, name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.active_parent_category_id in (SELECT id FROM scm_item_categories ipc
                WHERE ipc.user_category_id=uc.id)
            ) as subCategoryCount,
            (SELECT COUNT(*) FROM user_items ui WHERE ui.created_by_id = :userId
            AND ui.category_id IN (SELECT id FROM scm_item_categories ipc WHERE ipc.user_category_id=uc.id)
            ) as productCount,
            category_status as status,
            (select count(id) from scm_item_categories ic WHERE ic.active=1 AND ic.user_category_id = uc.id) as active
            FROM user_categories uc
            WHERE uc.created_by_id=:userId AND (uc.parent_category_id IS NULL AND uc.active_parent_category_id IS NULL)
            AND uc.category_status IN ('VERIFIED','APPROVED','COMPLETED','REJECTED','MERGED')
            """;
    public static final String COUNT_CLOSED_CATEGORIES = COUNT_START+CLOSED_CATEGORIES+COUNT_END;

    public static final String PENDING_APPROVAL_FROM_STORE_CATEGORIES="""
            select uc.id as id, e.employee_name as employeeName, uc.name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.parent_category_id=uc.id
            ) as subCategoryCount,
            0 as productCount,
            CASE WHEN :categoryId IS NOT NULL THEN
            (select name from scm_item_categories sic WHERE sic.id = :categoryId)
            ELSE 
            (select name from scm_item_categories sic WHERE sic.id = uc.active_parent_category_id)
            END as parentCategoryName,
            sws.store_name as storeName,
            sw.name as warehouseName,
            'PENDING' as status
            FROM user_categories uc
            LEFT JOIN scm_warehouse_stores sws ON sws.id = uc.store_id
            LEFT JOIN scm_warehouses sw ON sw.id = sws.warehouse_id
            LEFT JOIN acl_users e ON uc.created_by_id=e.id
            WHERE 
            (COALESCE(:warehouseId) IS NULL OR sws.warehouse_id IN :warehouseId)
            AND 
            (:warehouseStoreId IS NULL OR sws.id = :warehouseStoreId)
            AND
            (
            (:categoryId IS NULL AND uc.active_parent_category_id IS NULL)
            OR
            (:categoryId IS NOT NULL AND uc.active_parent_category_id = :categoryId)
            )
            AND (:name IS NULL OR uc.name LIKE CONCAT('%',:name,'%'))
            AND uc.category_status IN ('VERIFIED','APPROVED','COMPLETED','PENDING')
            AND (uc.is_approved_by_store IS NULL OR uc.is_approved_by_store=false)
            """;
    public static final String COUNT_PENDING_APPROVAL_FROM_STORE_CATEGORIES = COUNT_START+PENDING_APPROVAL_FROM_STORE_CATEGORIES+COUNT_END;

    public static final String PENDING_APPROVAL_FROM_STORE_SUB_CATEGORIES="""
            select uc.id as id, e.employee_name as employeeName, uc.name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.parent_category_id=uc.id
            ) as subCategoryCount,
            0 as productCount,
            CASE WHEN :categoryId IS NOT NULL THEN
            (select name from scm_item_categories sic WHERE sic.id = :categoryId)
            ELSE 
            (select name from scm_item_categories sic WHERE sic.id = uc.active_parent_category_id)
            END as parentCategoryName,
            sws.store_name as storeName,
            sw.name as warehouseName,
            'PENDING' as status
            FROM user_categories uc
            LEFT JOIN scm_warehouse_stores sws ON sws.id = uc.store_id
            LEFT JOIN scm_warehouses sw ON sw.id = sws.warehouse_id
            LEFT JOIN acl_users e ON uc.created_by_id=e.id
            WHERE 
            (:warehouseId IS NULL OR sws.warehouse_id = :warehouseId)
            AND 
            (:warehouseStoreId IS NULL OR sws.id = :warehouseStoreId)
            AND
            (
            (:categoryId IS NULL AND uc.active_parent_category_id IS NOT NULL)
            OR
            (:categoryId IS NOT NULL AND uc.active_parent_category_id = :categoryId)
            )
            AND (:name IS NULL OR uc.name LIKE CONCAT('%',:name,'%'))
            AND uc.category_status IN ('VERIFIED','APPROVED','COMPLETED','PENDING')
            AND (uc.is_approved_by_store IS NULL OR uc.is_approved_by_store=false)
            """;
    public static final String COUNT_PENDING_APPROVAL_FROM_STORE_SUB_CATEGORIES = COUNT_START+PENDING_APPROVAL_FROM_STORE_SUB_CATEGORIES+COUNT_END;

    public static final String CLOSED_SUB_CATEGORIES="""
            select uc.id as id, puc.name as categoryName, uc.name as subCategoryName, 
            (SELECT COUNT(*) FROM user_items ui WHERE
            ui.sub_category_id IN (SELECT id from scm_item_categories ic WHERE ic.user_category_id = uc.id)
            AND ui.created_by_id = :userId ) as productCount,
            uc.category_status as status,
            (select count(id) from scm_item_categories ic WHERE ic.active=1 AND ic.user_category_id = uc.id) as active
             FROM user_categories uc
             LEFT JOIN scm_item_categories puc ON (puc.id = uc.parent_category_id OR puc.id = uc.active_parent_category_id)
             WHERE uc.created_by_id=:userId 
             AND (uc.active_parent_category_id IS NOT NULL OR uc.parent_category_id IS NOT NULL)
             AND (:categoryId IS NULL OR uc.active_parent_category_id = :categoryId)
             AND uc.category_status IN ('VERIFIED','APPROVED','COMPLETED','REJECTED','MERGED')
            """;
    public static final String COUNT_CLOSED_SUB_CATEGORIES = COUNT_START+CLOSED_SUB_CATEGORIES+COUNT_END;


    public static final String GET_LIST_CATEGORIES="""
            SELECT 'STORE_MANAGED' as `type`, scws.warehouse_store_id as storeId, ic.user_category_id as userCategoryId, ic.id,ic.name,ic.code 
            FROM scm_item_categories ic
            LEFT JOIN scm_category_warehouse_stores scws ON scws.category_id = ic.id
            WHERE 
            ic.active=1 AND ic.category_status IN ('APPROVED')
            AND (:storeId IS NULL OR scws.warehouse_store_id = :storeId)
            AND (COALESCE(:warehouseId) IS NULL OR scws.warehouse_id IN (:warehouseId))
            AND (:name IS NULL OR LOWER(ic.name) LIKE LOWER(CONCAT('%',:name,'%')))
            AND (:code IS NULL OR LOWER(ic.code) LIKE LOWER(CONCAT('%',:code,'%')))
            AND ic.parent_category_id IS NULL
            """;


    public static final String GET_LIST_SUB_CATEGORIES="""
            SELECT 'STORE_MANAGED' as `type`, ic.user_category_id as userCategoryId, ic.id,ic.name,ic.code 
            FROM scm_item_categories ic
            LEFT JOIN scm_category_warehouse_stores scws ON scws.category_id = ic.id
            WHERE 
            ic.active=1 AND ic.category_status IN ('APPROVED')
            AND (COALESCE(:warehouseId) IS NULL OR scws.warehouse_id IN (:warehouseId))
            AND (:name IS NULL OR LOWER(ic.name) LIKE LOWER(CONCAT('%',:name,'%')))
            AND (:code IS NULL OR LOWER(ic.code) LIKE LOWER(CONCAT('%',:code,'%')))
            AND ic.parent_category_id = :categoryId
            """;
}
