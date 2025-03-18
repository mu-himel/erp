package com.agi.aesl.erpscm.inventory.user_request.repository;

public class UserItemQuery {
    private UserItemQuery(){}
    public static final String COUNT_START="SELECT COUNT(*) FROM (";
    public static final String COUNT_END=") AS TOTAL";
    public static final String GET_MY_LIST = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
             ui.item_status as status
             FROM user_items ui
             LEFT JOIN scm_item_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN scm_item_categories cat ON ui.category_id = cat.id
             WHERE ui.created_by_id=:userId
             AND ui.item_status NOT IN ('VERIFIED','APPROVED','COMPLETED','REJECTED','MERGED')
             AND (:categoryId IS NULL OR ui.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR ui.sub_category_id = :subCategoryId)
            """;
    public static final String COUNT_MY_LIST=COUNT_START+GET_MY_LIST+COUNT_END;

    public static final String GET_PENDING_VERIFICATIONS = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
             CASE WHEN ui.item_status != 'REVIEW' AND (uih.id IS NOT NULL AND uih.employee_id = :userId) THEN
                    uih.item_status
                ELSE
                    ui.item_status
            END  as status,
             FROM user_items ui
             LEFT JOIN scm_item_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN scm_item_categories cat ON ui.category_id = cat.id
             LEFT JOIN user_item_histories uih ON uih.user_item_id = ui.id
             WHERE (
                (ui.next_verifier_id=:userId AND ui.item_status IN ('PENDING_VERIFICATION','REVIEW','VERIFIED'))
                    OR
                (uih.employee_id = :userId AND uih.item_status = 'VERIFIED')
                )
             AND (:categoryId IS NULL OR ui.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR ui.sub_category_id = :subCategoryId)
            """;

    public static final String COUNT_PENDING_VERIFICATIONS = COUNT_START+ GET_PENDING_VERIFICATIONS+COUNT_END;

    public static final String GET_PENDING_APPROVALS = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
             CASE WHEN ui.item_status != 'REVIEW' AND (uih.id IS NOT NULL AND uih.employee_id = :userId) THEN
                    uih.item_status
                ELSE
                    ui.item_status
             END  as status,
             FROM user_items ui
             LEFT JOIN scm_item_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN scm_item_categories cat ON ui.category_id = cat.id
             LEFT JOIN user_item_histories uih ON uih.user_item_id = ui.id
             WHERE (
                (ui.next_approver_id=:userId AND ui.item_status IN ('PENDING_APPROVAL','REVIEW','APPROVED'))
                    OR 
                (uih.employee_id = :userId AND uih.item_status = 'APPROVED')
                )
             AND (:categoryId IS NULL OR ui.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR ui.sub_category_id = :subCategoryId)
            """;

    public static final String COUNT_PENDING_APPROVALS = COUNT_START+ GET_PENDING_APPROVALS+COUNT_END;

    public static final String GET_CLOSED = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
             ui.item_status as status
             FROM user_items ui
             LEFT JOIN scm_item_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN scm_item_categories cat ON ui.category_id = cat.id
             WHERE ui.created_by_id=:userId 
             AND (:categoryId IS NULL OR ui.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR ui.sub_category_id = :subCategoryId)
             AND ui.item_status IN ('VERIFIED','APPROVED','COMPLETED','REJECTED','MERGED')
            """;

    public static final String COUNT_CLOSED = COUNT_START+ GET_CLOSED+COUNT_END;

    public static final String GET_PENDING_APPROVALS_BY_STORE = """
            select ui.id as id, e.employee_name as employeeName, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,sws.store_name as storeName,sw.name as warehouseName,
             'PENDING' as status, ui.name as brandName
             FROM user_items ui
             LEFT JOIN scm_warehouse_stores sws ON sws.id = ui.warehouse_store_id
             LEFT JOIN scm_warehouses sw ON sw.id = sws.warehouse_id
             LEFT JOIN scm_item_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN scm_item_categories cat ON ui.category_id = cat.id
             LEFT JOIN acl_users e ON ui.created_by_id = e.id
             WHERE (:warehouseId IS NULL OR sws.warehouse_id = :warehouseId)
             AND (:warehouseStoreId IS NULL OR sws.id = :warehouseStoreId)
             AND (:categoryId IS NULL OR ui.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR ui.sub_category_id = :subCategoryId)
             AND ui.item_status IN ('VERIFIED','APPROVED','COMPLETED','PENDING')
             AND (ui.is_approved_by_store IS NULL OR ui.is_approved_by_store=false)
            """;
    public static final String COUNT_PENDING_APPROVALS_BY_STORE = COUNT_START+ GET_PENDING_APPROVALS_BY_STORE+COUNT_END;
}
