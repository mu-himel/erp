package com.agi.aesl.erpscm.inventory.user_request.repository;

public interface UserCategoryQuery {

    String getMyCategories = """
            select id as id, name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.parent_category_id=uc.id
            ) as subCategoryCount, 0 as productCount,
            (select count(id) from scm_item_categories ic WHERE ic.active=1 AND ic.user_category_id = uc.id) as active, 
            category_status as status
            FROM user_categories uc
            WHERE uc.created_by_id=:userId 
            AND uc.category_status NOT IN ('COMPLETED','REJECTED')
            AND (uc.parent_category_id IS NULL AND uc.active_parent_category_id IS NULL)
            """;
    String countMyCategories="SELECT COUNT(*) FROM ("+ getMyCategories+") as total";

    String getMySubCategories= """
            select uc.id as id, puc.name as categoryName, uc.name as subCategoryName, 0 as productCount,
            uc.category_status as status
             FROM user_categories uc
             LEFT JOIN user_categories puc ON puc.id = uc.parent_category_id
             WHERE uc.created_by_id=:userId AND uc.parent_category_id = :categoryId
            """;

    String getGetMySubCategories = """
            select uc.id as id, 
            (select count(id) from scm_item_categories ic WHERE ic.active=1 AND ic.user_category_id = uc.id) as active, 
            puc.name as categoryName, uc.name as subCategoryName, 0 as productCount,
            uc.category_status as status
             FROM user_categories uc
             LEFT JOIN scm_item_categories puc ON (puc.id = uc.parent_category_id OR puc.id = uc.active_parent_category_id )
             WHERE uc.created_by_id=:userId 
             AND uc.category_status NOT IN ('COMPLETED','REJECTED')
             AND (uc.active_parent_category_id IS NOT NULL OR uc.parent_category_id IS NOT NULL)
             AND (:categoryId IS NULL OR uc.parent_category_id = :categoryId OR uc.active_parent_category_id = :categoryId)
            """;
    String countGetMySubCategories = "SELECT COUNT(*) FROM ("+getGetMySubCategories+") as total";

    String getCategoryPVs = """
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
    String countCategoryPVs = "SELECT COUNT(*) FROM ("+getCategoryPVs+") as total";

    String getSubCategoryPVs="""
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
    String countSubCategoryPVs = "SELECT COUNT(*) FROM ("+getSubCategoryPVs+") as total";

    String getCategoryPAs="""
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
    String countCategoryPAs = "SELECT COUNT(*) FROM ("+getCategoryPAs+") as total";

    String getSubCategoryPAs="""
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
    String countSubCategoryPAs = "SELECT COUNT(*) FROM ("+getSubCategoryPAs+") as total";

    String closedCategories="""
            select uc.id as id, name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.parent_category_id=uc.id
            ) as subCategoryCount, 0 as productCount,
            category_status as status,
            (select count(id) from scm_item_categories ic WHERE ic.active=1 AND ic.user_category_id = uc.id) as active
            FROM user_categories uc
            WHERE uc.created_by_id=:userId AND (uc.parent_category_id IS NULL AND uc.active_parent_category_id IS NULL)
            AND uc.category_status IN ('VERIFIED','APPROVED','COMPLETED')
            """;
    String countClosedCategories = "SELECT COUNT(*) FROM ("+closedCategories+") as total";

    String pendingApprovalFromStoreCategories="""
            select uc.id as id, e.employee_name as employeeName, name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.parent_category_id=uc.id
            ) as subCategoryCount, 
            0 as productCount,
            CASE WHEN :categoryId IS NOT NULL THEN
            (select name from scm_item_categories sic WHERE sic.id = :categoryId)
            ELSE '' END as parentCategoryName,
            'PENDING' as status
            FROM user_categories uc
            LEFT JOIN scm_warehouse_stores sws ON sws.id = uc.store_id
            LEFT JOIN acl_users e ON uc.created_by_id=e.id
            WHERE 
            (:warehouseId IS NULL OR sws.warehouse_id = :warehouseId)
            AND 
            (:warehouseStoreId IS NULL OR sws.id = :warehouseStoreId)
            AND
            (
            (:categoryId IS NULL AND uc.active_parent_category_id IS NULL)
            OR
            (:categoryId IS NOT NULL AND uc.active_parent_category_id = :categoryId)
            )
            AND uc.category_status IN ('VERIFIED','APPROVED','COMPLETED','PENDING')
            AND (uc.is_approved_by_store IS NULL OR uc.is_approved_by_store=false)
            """;
    String countPendingApprovalByStoreCategories = "SELECT COUNT(*) FROM ("+pendingApprovalFromStoreCategories+") as total";

    String closedSubCategories="""
            select uc.id as id, puc.name as categoryName, uc.name as subCategoryName, 0 as productCount,
            uc.category_status as status,
            (select count(id) from scm_item_categories ic WHERE ic.active=1 AND ic.user_category_id = uc.id) as active
             FROM user_categories uc
             LEFT JOIN scm_item_categories puc ON (puc.id = uc.parent_category_id OR puc.id = uc.active_parent_category_id)
             WHERE uc.created_by_id=:userId 
             AND (uc.active_parent_category_id IS NOT NULL OR uc.parent_category_id IS NOT NULL)
             AND (:categoryId IS NULL OR uc.active_parent_category_id = :categoryId)
             AND uc.category_status IN ('VERIFIED','APPROVED','COMPLETED')
            """;
    String countClosedSubCategories = "SELECT COUNT(*) FROM ("+closedSubCategories+") as total";

//    SELECT 'USER_MANAGED' as `type`, ua.id, ua.name, ua.code FROM user_categories ua
    //            WHERE (:name IS NULL OR LOWER(ua.name) LIKE LOWER(CONCAT('%',:name,'%')))
//            AND (:code IS NULL OR LOWER(ua.code) LIKE LOWER(CONCAT('%',:code,'%')))
//            AND ua.created_by_id = :userId
//            UNION
    String getListCategories="""
            SELECT 'STORE_MANAGED' as `type`, ic.user_category_id as userCategoryId, ic.id,ic.name,ic.code from scm_item_categories ic
            LEFT JOIN scm_category_warehouse_stores scws ON scws.category_id = ic.id
            WHERE 
            (:storeId IS NULL OR scws.warehouse_store_id = :storeId)
            AND (:name IS NULL OR LOWER(ic.name) LIKE LOWER(CONCAT('%',:name,'%')))
            AND (:code IS NULL OR LOWER(ic.code) LIKE LOWER(CONCAT('%',:code,'%')))
            AND ic.parent_category_id IS NULL
            """;

//    SELECT 'USER_MANAGED' as `type`, ua.id, ua.name, ua.code FROM user_categories ua
//    WHERE (:name IS NULL OR LOWER(ua.name) LIKE LOWER(CONCAT('%',:name,'%')))
//    AND (:code IS NULL OR LOWER(ua.code) LIKE LOWER(CONCAT('%',:code,'%')))
//    AND ua.parent_category_id = :categoryId
//    AND ua.created_by_id = :userId
//            UNION
    String getListSubCategories="""
            SELECT 'STORE_MANAGED' as `type`, ic.user_category_id as userCategoryId, ic.id,ic.name,ic.code from scm_item_categories ic
            WHERE (:name IS NULL OR LOWER(ic.name) LIKE LOWER(CONCAT('%',:name,'%')))
            AND (:code IS NULL OR LOWER(ic.code) LIKE LOWER(CONCAT('%',:code,'%')))
            AND ic.parent_category_id = :categoryId
            """;
}
