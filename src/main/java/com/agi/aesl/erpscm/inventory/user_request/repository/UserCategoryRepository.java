package com.agi.aesl.erpscm.inventory.user_request.repository;

import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategory;
import com.agi.aesl.erpscm.inventory.user_request.enums.UserCategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.agi.aesl.erpscm.inventory.user_request.repository.UserCategoryQuery.*;

@Repository
public interface UserCategoryRepository extends JpaRepository<UserCategory,Long> {

    @Query(value = GET_MY_CATEGORIES, countQuery = COUNT_MY_CATEGORIES, nativeQuery = true)
    Page<UserCategory> findAllCategoryByCreatedById(String userId, Pageable pageable);

    @Query(value = GET_MY_SUB_CATEGORIES, countQuery = COUNT_MY_SUB_CATEGORIES, nativeQuery = true)
    Page<UserSubCategory> findAllSubCategoryByCreatedById(String userId,Long categoryId, Pageable pageable);

    Boolean existsByName(String name);

    @Query(value = GET_CATEGORY_PVS,countQuery = COUNT_CATEGORY_PVS,nativeQuery = true)
    Page<UserCategory> findAllCategoryByNextVerifierId(String userId, Pageable pageable);

    @Query(value = GET_SUB_CATEGORY_PVS,countQuery = COUNT_SUB_CATEGORY_PVS, nativeQuery = true)
    Page<UserSubCategory> findAllSubCategoryByNextVerifierId(String userId,Long categoryId, Pageable pageable);

    @Query(value = GET_CATEGORY_PAS,countQuery = COUNT_CATEGORY_PAS, nativeQuery = true)
    Page<UserCategory> findAllCategoryByNextApproverId(String userId, Pageable pageable);

    @Query(value = GET_SUB_CATEGORY_PAS, countQuery = COUNT_SUB_CATEGORY_PAS, nativeQuery = true)
    Page<UserSubCategory> findAllSubCategoryByNextApproverId(String userId, Long categoryId, Pageable pageable);

    @Query(value = CLOSED_CATEGORIES,countQuery = COUNT_CLOSED_CATEGORIES, nativeQuery = true)
    Page<UserCategory> findAllClosed(String userId, Pageable pageable);
    @Query(value = PENDING_APPROVAL_FROM_STORE_CATEGORIES,countQuery = COUNT_PENDING_APPROVAL_FROM_STORE_CATEGORIES, nativeQuery = true)
    Page<PendingApprovalStore> findAllPendingApprovalByStore(Long categoryId,
                                                             String name,
                                                             Long warehouseId,Long warehouseStoreId,
                                                             Pageable pageable);

    @Query(value = PENDING_APPROVAL_FROM_STORE_SUB_CATEGORIES,countQuery = COUNT_PENDING_APPROVAL_FROM_STORE_SUB_CATEGORIES, nativeQuery = true)
    Page<PendingApprovalStore> findAllPendingApprovalSubCatByStore(Long categoryId,
                                                             String name,
                                                             Long warehouseId,Long warehouseStoreId,
                                                             Pageable pageable);

    @Query(value = CLOSED_SUB_CATEGORIES,countQuery = COUNT_CLOSED_SUB_CATEGORIES, nativeQuery = true)
    Page<UserSubCategory> findAllClosedSubCategory(String userId,Long categoryId, Pageable pageable);

    @Query(value = GET_LIST_CATEGORIES,nativeQuery = true)
    List<UserCategoryInfo> getAllCategories(String name, String code, Long warehouseId, Long storeId);

    @Query(value = GET_LIST_SUB_CATEGORIES ,nativeQuery = true)
    List<UserCategoryInfo> getAllSubCategories(Long categoryId, String name, String code, Long warehouseId);

    interface UserCategoryInfo{

        String getType();
        Long getId();
        Long getUserCategoryId();
        Long getStoreId();
        String getCode();
        String getName();


    }

    interface PendingApprovalStore extends UserCategory{
        String getEmployeeName();
        String getParentCategoryName();
        String getWarehouseName();
        String getStoreName();
    }

    interface UserCategory{
        Long getId();
        String getCategoryName();
        Long getSubCategoryCount();
        Long getProductCount();

        Integer getActive();
        UserCategoryStatus getStatus();
    }

    interface UserSubCategory{
        Long getId();
        String getCategoryName();
        String getSubCategoryName();
        Long getProductCount();

        Integer getActive();
        UserCategoryStatus getStatus();
    }
}
