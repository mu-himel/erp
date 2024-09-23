package com.agi.aesl.erpscm.inventory.user_request.repository;

public interface UserItemQuery {

    String getMyList = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
            uc.item_status as status
             FROM user_items ui
             LEFT JOIN user_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN user_categories cat ON ui.category_id = cat.id
             WHERE uc.created_by_id=:userId
             AND (:categoryId IS NULL OR uc.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR uc.sub_category_id = :subCategoryId)
            """;
    String countMyList="SELECT COUNT(*) FROM ("+getMyList+") as total";

    String getPendingVerifications = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
            uc.item_status as status
             FROM user_items ui
             LEFT JOIN user_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN user_categories cat ON ui.category_id = cat.id
             WHERE ui.next_verifier_id=:userId
             AND (:categoryId IS NULL OR uc.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR uc.sub_category_id = :subCategoryId)
            """;

    String countPendingVerifications = "SELECT COUNT(*) FROM ("+ getPendingVerifications+") as total";

    String getPendingApprovals = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
            uc.item_status as status
             FROM user_items ui
             LEFT JOIN user_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN user_categories cat ON ui.category_id = cat.id
             WHERE ui.next_approver_id=:userId
             AND (:categoryId IS NULL OR uc.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR uc.sub_category_id = :subCategoryId)
            """;

    String countPendingApprovals = "SELECT COUNT(*) FROM ("+ getPendingApprovals+") as total";

    String getClosed = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
            uc.item_status as status
             FROM user_items ui
             LEFT JOIN user_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN user_categories cat ON ui.category_id = cat.id
             WHERE ui.created_by_id=:userId 
             AND (:categoryId IS NULL OR uc.category_id = :categoryId)
             AND (:subCategoryId IS NULL OR uc.sub_category_id = :subCategoryId)
             AND ui.item_status IN ('VERIFIED','APPROVED','COMPLETED')
            """;

    String countClosed = "SELECT COUNT(*) FROM ("+ getClosed+") as total";
}
