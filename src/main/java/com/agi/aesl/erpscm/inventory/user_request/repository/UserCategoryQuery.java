package com.agi.aesl.erpscm.inventory.user_request.repository;

public interface UserCategoryQuery {

    String getMyCategories = """
            select id as id, name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.parent_category_id=uc.id
            ) as subCategoryCount, 0 as productCount,
            category_status as status
            FROM user_categories uc
            WHERE uc.created_by_id=:userId AND uc.parent_category_id IS NULL
            """;
    String countMyCategories="SELECT COUNT(*) FROM ("+ getMyCategories+") as total";

    String getMySubCategories= """
            select uc.id as id, puc.name as categoryName, uc.name as subCategoryName, 0 as productCount,
            uc.category_status as status
             FROM user_categories uc
             LEFT JOIN user_categories puc ON puc.id = uc.parent_category_id
             WHERE uc.created_by_id=:userId AND uc.parent_category_id IS NOT NULL
            """;
}
