package com.agi.aesl.erpscm.inventory.user_request.repository;

public interface UserItemQuery {

    String getMyList = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
             ui.item_status as status
             FROM user_items ui
             LEFT JOIN user_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN user_categories cat ON ui.category_id = cat.id
             WHERE ui.created_by_id=:userId
             AND (:categoryId IS NULL OR ui.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR ui.sub_category_id = :subCategoryId)
            """;
    String countMyList="SELECT COUNT(*) FROM ("+getMyList+") as total";

    String getPendingVerifications = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
             CASE WHEN ui.item_status != 'REVIEW' AND (uih.id IS NOT NULL AND uih.employee_id = :userId) THEN
                    uih.item_status
                ELSE
                    ui.item_status
            END  as status,
             FROM user_items ui
             LEFT JOIN user_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN user_categories cat ON ui.category_id = cat.id
             LEFT JOIN user_item_histories uih ON uih.user_item_id = ui.id
             WHERE (
                (ui.next_verifier_id=:userId AND ui.item_status IN ('PENDING_VERIFICATION','REVIEW','VERIFIED'))
                    OR
                (uih.employee_id = :userId AND uih.item_status = 'VERIFIED')
                )
             AND (:categoryId IS NULL OR ui.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR ui.sub_category_id = :subCategoryId)
            """;

    String countPendingVerifications = "SELECT COUNT(*) FROM ("+ getPendingVerifications+") as total";

    String getPendingApprovals = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
             CASE WHEN ui.item_status != 'REVIEW' AND (uih.id IS NOT NULL AND uih.employee_id = :userId) THEN
                    uih.item_status
                ELSE
                    ui.item_status
             END  as status,
             FROM user_items ui
             LEFT JOIN user_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN user_categories cat ON ui.category_id = cat.id
             LEFT JOIN user_item_histories uih ON uih.user_item_id = ui.id
             WHERE (
                (ui.next_approver_id=:userId AND ui.item_status IN ('PENDING_APPROVAL','REVIEW','APPROVED'))
                    OR 
                (uih.employee_id = :userId AND uih.item_status = 'APPROVED')
                )
             AND (:categoryId IS NULL OR ui.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR ui.sub_category_id = :subCategoryId)
            """;

    String countPendingApprovals = "SELECT COUNT(*) FROM ("+ getPendingApprovals+") as total";

    String getClosed = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
             ui.item_status as status
             FROM user_items ui
             LEFT JOIN user_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN user_categories cat ON ui.category_id = cat.id
             WHERE ui.created_by_id=:userId 
             AND (:categoryId IS NULL OR ui.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR ui.sub_category_id = :subCategoryId)
             AND ui.item_status IN ('VERIFIED','APPROVED','COMPLETED')
            """;

    String countClosed = "SELECT COUNT(*) FROM ("+ getClosed+") as total";

    String getPendingApprovalsByStore = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
             ui.item_status as status
             FROM user_items ui
             LEFT JOIN user_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN user_categories cat ON ui.category_id = cat.id
             WHERE ui.created_by_id=:userId 
             AND (:categoryId IS NULL OR ui.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR ui.sub_category_id = :subCategoryId)
             AND ui.item_status IN ('VERIFIED','APPROVED','COMPLETED')
             AND (ui.is_approved_by_store IS NULL OR ui.is_approved_by_store=false)
            """;
    String countPendingApprovalsByStore = "SELECT COUNT(*) FROM ("+ getPendingApprovalsByStore+") as total";
}
