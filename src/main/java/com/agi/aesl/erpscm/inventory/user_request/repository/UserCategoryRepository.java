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

    @Query(value = getGetMySubCategories, countQuery = countGetMySubCategories, nativeQuery = true)
    Page<UserSubCategory> findAllSubCategoryByCreatedById(String userId,Long categoryId, Pageable pageable);

    Boolean existsByName(String name);

    @Query(value = getCategoryPVs,countQuery = countCategoryPVs,nativeQuery = true)
    Page<UserCategory> findAllCategoryByNextVerifierId(String userId, Pageable pageable);

    @Query(value = getSubCategoryPVs,countQuery = countSubCategoryPVs, nativeQuery = true)
    Page<UserSubCategory> findAllSubCategoryByNextVerifierId(String userId,Long categoryId, Pageable pageable);

    @Query(value = getCategoryPAs,countQuery = countCategoryPAs, nativeQuery = true)
    Page<UserCategory> findAllCategoryByNextApproverId(String userId, Pageable pageable);

    @Query(value = getSubCategoryPAs, countQuery = countSubCategoryPAs, nativeQuery = true)
    Page<UserSubCategory> findAllSubCategoryByNextApproverId(String userId, Long categoryId, Pageable pageable);

    @Query(value = closedCategories,countQuery = countClosedCategories, nativeQuery = true)
    Page<UserCategory> findAllClosed(String userId, Pageable pageable);
    @Query(value = pendingApprovalFromStoreCategories,countQuery = countPendingApprovalByStoreCategories, nativeQuery = true)
    Page<PendingApprovalStore> findAllPendingApprovalByStore(String userId,Long categoryId, Pageable pageable);

    @Query(value = closedSubCategories,countQuery = countClosedSubCategories, nativeQuery = true)
    Page<UserSubCategory> findAllClosedSubCategory(String userId,Long categoryId, Pageable pageable);

    @Query(value = getListCategories,nativeQuery = true)
    List<UserCategoryInfo> getAllCategories(String userId, String name, String code);

    @Query(value = getListSubCategories ,nativeQuery = true)
    List<UserCategoryInfo> getAllSubCategories(String userId, Long categoryId, String name, String code);

    interface UserCategoryInfo{

        String getType();
        Long getId();
        String getCode();
        String getName();


    }

    interface PendingApprovalStore extends UserCategory{
        String getEmployeeName();
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
