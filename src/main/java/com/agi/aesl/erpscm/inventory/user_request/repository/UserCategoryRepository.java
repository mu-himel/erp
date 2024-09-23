package com.agi.aesl.erpscm.inventory.user_request.repository;

import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategory;
import com.agi.aesl.erpscm.inventory.user_request.enums.UserCategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCategoryRepository extends JpaRepository<UserCategory,Long>, UserCategoryQuery {

    @Query(value = getMyCategories, countQuery = countMyCategories, nativeQuery = true)
    Page<UserCategory> findAllCategoryByCreatedById(String userId, Pageable pageable);

    @Query(value = """
            select uc.id as id, puc.name as categoryName, uc.name as subCategoryName, 0 as productCount,
            uc.category_status as status
             FROM user_categories uc
             LEFT JOIN user_categories puc ON puc.id = uc.parent_category_id
             WHERE uc.created_by_id=:userId AND uc.parent_category_id = :categoryId
            """, nativeQuery = true)
    Page<UserSubCategory> findAllSubCategoryByCreatedById(String userId,Long categoryId, Pageable pageable);

    Boolean existsByName(String name);

    @Query(value = """
            select id as id, name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.parent_category_id=uc.id
            ) as subCategoryCount, 0 as productCount,
            category_status as status
            FROM user_categories uc
            WHERE uc.next_verifier_id=:userId AND uc.parent_category_id IS NULL
            """, nativeQuery = true)
    Page<UserCategory> findAllCategoryByNextVerifierId(String userId, Pageable pageable);

    @Query(value = """
            select uc.id as id, puc.name as categoryName, uc.name as subCategoryName, 0 as productCount,
            uc.category_status as status
             FROM user_categories uc
             LEFT JOIN user_categories puc ON puc.id = uc.parent_category_id
             WHERE uc.next_verifier_id=:userId AND uc.parent_category_id IS NOT NULL
            """, nativeQuery = true)
    Page<UserSubCategory> findAllSubCategoryByNextVerifierId(String userId, Pageable pageable);

    @Query(value = """
            select id as id, name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.parent_category_id=uc.id
            ) as subCategoryCount, 0 as productCount,
            category_status as status
            FROM user_categories uc
            WHERE uc.next_approver_id=:userId AND uc.parent_category_id IS NULL
            """, nativeQuery = true)
    Page<UserCategory> findAllCategoryByNextApproverId(String userId, Pageable pageable);

    @Query(value = """
            select uc.id as id, puc.name as categoryName, uc.name as subCategoryName, 0 as productCount,
            uc.category_status as status
             FROM user_categories uc
             LEFT JOIN user_categories puc ON puc.id = uc.parent_category_id
             WHERE uc.next_approver_id=:userId AND uc.parent_category_id IS NOT NULL
            """, nativeQuery = true)
    Page<UserSubCategory> findAllSubCategoryByNextApproverId(String userId, Pageable pageable);

    @Query(value = """
            select id as id, name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.parent_category_id=uc.id
            ) as subCategoryCount, 0 as productCount,
            category_status as status
            FROM user_categories uc
            WHERE uc.created_by_id=:userId AND uc.parent_category_id IS NULL
            AND uc.category_status IN ('VERIFIED','APPROVED','COMPLETED')
            """, nativeQuery = true)
    Page<UserCategory> findAllClosed(String userId, Pageable pageable);

    @Query(value = """
            select uc.id as id, puc.name as categoryName, uc.name as subCategoryName, 0 as productCount,
            uc.category_status as status
             FROM user_categories uc
             LEFT JOIN user_categories puc ON puc.id = uc.parent_category_id
             WHERE uc.created_by_id=:userId AND uc.parent_category_id IS NOT NULL
             AND uc.category_status IN ('VERIFIED','APPROVED','COMPLETED')
            """, nativeQuery = true)
    Page<UserSubCategory> findAllClosedSubCategory(String userId, Pageable pageable);

    @Query(value = """
            SELECT 'USER_MANAGED' as `type`, ua.id, ua.name, ua.code FROM user_categories ua
            WHERE (:name IS NULL OR LOWER(ua.name) LIKE LOWER(CONCAT('%',:name,'%')))
            AND (:code IS NULL OR LOWER(ua.code) LIKE LOWER(CONCAT('%',:code,'%')))
            AND ua.created_by_id = :userId
            UNION
            SELECT 'STORE_MANAGED' as `type`, ic.id,ic.name,ic.code from scm_item_categories ic
            WHERE (:name IS NULL OR LOWER(ic.name) LIKE LOWER(CONCAT('%',:name,'%')))
            AND (:code IS NULL OR LOWER(ic.code) LIKE LOWER(CONCAT('%',:code,'%')))
            """,nativeQuery = true)
    List<UserCategoryInfo> getAllCategories(String userId, String name, String code);

    @Query(value = """
            SELECT 'USER_MANAGED' as `type`, ua.id, ua.name, ua.code FROM user_categories ua
            WHERE (:name IS NULL OR LOWER(ua.name) LIKE LOWER(CONCAT('%',:name,'%')))
            AND (:code IS NULL OR LOWER(ua.code) LIKE LOWER(CONCAT('%',:code,'%')))
            AND ua.parent_category_id = :categoryId
            AND ua.created_by_id = :userId
            UNION
            SELECT 'STORE_MANAGED' as `type`, ic.id,ic.name,ic.code from scm_item_categories ic
            WHERE (:name IS NULL OR LOWER(ic.name) LIKE LOWER(CONCAT('%',:name,'%')))
            AND (:code IS NULL OR LOWER(ic.code) LIKE LOWER(CONCAT('%',:code,'%')))
            AND ic.parent_category_id = :categoryId
            """,nativeQuery = true)
    List<UserCategoryInfo> getAllSubCategories(String userId, Long categoryId, String name, String code);

    interface UserCategoryInfo{

        String getType();
        Long getId();
        String getCode();
        String getName();


    }
    interface UserCategory{
        Long getId();
        String getCategoryName();
        Long getSubCategoryCount();
        Long getProductCount();
        UserCategoryStatus getStatus();
    }

    interface UserSubCategory{
        Long getId();
        String getCategoryName();
        String getSubCategoryName();
        Long getProductCount();
        UserCategoryStatus getStatus();
    }
}
