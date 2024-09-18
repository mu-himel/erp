package com.agi.aesl.erpscm.inventory.user_request.repository;

import com.agi.aesl.erpscm.inventory.user_request.entity.UserItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem,Long> {

    @Query(value = """
            SELECT * FROM (SELECT i.id, i.brand_id ,i.active,
                    GROUP_CONCAT(DISTINCT  ia.attribute_type,' ',ia.attribute_value , ' ',ia.attribute_unit ORDER BY ia.id ASC separator ' - ') itemAttributes
            FROM user_item_attributes ia
            LEFT JOIN user_items i on i.id=ia.item_id
            GROUP BY i.id) p
            WHERE p.brand_id=:brandId AND itemAttributes = :attribute
            """,nativeQuery = true)
    List<?> findByAttributes(Long brandId, String attribute);

    @Query(value = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
            uc.item_status as status
             FROM user_items ui
             LEFT JOIN user_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN user_categories cat ON ui.category_id = cat.id
             WHERE uc.created_by_id=:userId
            """, nativeQuery = true)
    Page<UserItem> findAllByCreatedById(String userId, Pageable pageable);

    @Query(value = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
            uc.item_status as status
             FROM user_items ui
             LEFT JOIN user_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN user_categories cat ON ui.category_id = cat.id
             WHERE ui.next_verifier_id=:userId
            """, nativeQuery = true)
    Page<UserItem> findAllPendingVerifications(String userId, Pageable pageable);

    @Query(value = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
            uc.item_status as status
             FROM user_items ui
             LEFT JOIN user_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN user_categories cat ON ui.category_id = cat.id
             WHERE ui.next_approver_id=:userId
            """, nativeQuery = true)
    Page<UserItem> findAllPendingApprovals(String userId, Pageable pageable);

    @Query(value = """
            select ui.id as id, cat.name as categoryName, subCat.name as subCategoryName,
             ui.item_attribute_name as productName,
            uc.item_status as status
             FROM user_items ui
             LEFT JOIN user_categories subCat ON ui.sub_category_id = subCat.id
             LEFT JOIN user_categories cat ON ui.category_id = cat.id
             WHERE ui.created_by_id=:userId AND ui.item_status IN ('VERIFIED','APPROVED','COMPLETED')
            """, nativeQuery = true)
    Page<UserItem> findAllClosed(String userId, Pageable pageable);

    interface UserItem{
        Long getId();
        String getCategoryName();
        String getSubCategoryName();
        String getProductName();
        String getStatus();

    }
}
