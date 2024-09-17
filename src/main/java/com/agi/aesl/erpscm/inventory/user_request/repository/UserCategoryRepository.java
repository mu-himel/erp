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
public interface UserCategoryRepository extends JpaRepository<UserCategory,Long> {

    @Query(value = """
            select id as id, name as categoryName, (
                SELECT COUNT(*) FROM user_categories uc1
                WHERE uc1.parent_category_id=uc.id
            ) as subCategoryCount, 0 as productCount,
            category_status as status
            FROM user_categories uc
            WHERE uc.created_by_id=:userId AND uc.parent_category_id IS NULL
            """, nativeQuery = true)
    Page<UserCategory> findAllCategoryByCreatedById(String userId, Pageable pageable);

    @Query(value = """
            select uc.id as id, puc.name as categoryName, uc.name as subCategoryName, 0 as productCount,
            uc.category_status as status
             FROM user_categories uc
             LEFT JOIN user_categories puc ON puc.id = uc.parent_category_id
             WHERE uc.created_by_id=:userId AND uc.parent_category_id IS NOT NULL
            """, nativeQuery = true)
    Page<UserSubCategory> findAllSubCategoryByCreatedById(String userId, Pageable pageable);

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
