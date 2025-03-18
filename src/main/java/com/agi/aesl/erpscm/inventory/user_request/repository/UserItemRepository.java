package com.agi.aesl.erpscm.inventory.user_request.repository;

import com.agi.aesl.erpscm.inventory.user_request.entity.UserItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.agi.aesl.erpscm.inventory.user_request.repository.UserItemQuery.*;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem,Long> {

    @Query(value = """
            SELECT * FROM (SELECT i.id, i.brand_id as brandId ,i.active,
                    GROUP_CONCAT(DISTINCT  ia.attribute_type,' ',ia.attribute_value , ' ',ia.attribute_unit ORDER BY ia.id ASC separator ' - ') itemAttributes
            FROM user_item_attributes ia
            LEFT JOIN user_items i on i.id=ia.user_item_id
            GROUP BY i.id) p
            WHERE p.brand_id=:brandId AND itemAttributes = :attribute
            """,nativeQuery = true)
    List<UserItemByAttribute> findByAttributes(Long brandId, String attribute);

    @Query(value = GET_MY_LIST, countQuery = COUNT_MY_LIST, nativeQuery = true)
    Page<UserItem> findAllByCreatedById(String userId,Long categoryId,
                                        Long subCategoryId,Pageable pageable);

    @Query(value = GET_PENDING_VERIFICATIONS, countQuery = COUNT_PENDING_VERIFICATIONS,nativeQuery = true)
    Page<UserItem> findAllPendingVerifications(String userId,Long categoryId,
                                               Long subCategoryId,
                                               Pageable pageable);

    @Query(value = GET_PENDING_APPROVALS, countQuery =  COUNT_PENDING_APPROVALS, nativeQuery = true)
    Page<UserItem> findAllPendingApprovals(String userId, Long categoryId,
                                           Long subCategoryId,Pageable pageable);

    @Query(value = GET_CLOSED, countQuery =  COUNT_CLOSED, nativeQuery = true)
    Page<UserItem> findAllClosed(String userId, Long categoryId,
                                 Long subCategoryId,Pageable pageable);

    @Query(value = GET_PENDING_APPROVALS_BY_STORE, countQuery =  COUNT_PENDING_APPROVALS_BY_STORE, nativeQuery = true)
    Page<PendingApprovalUserItem> findAllPendingApprovalItemsByStore(Long categoryId,
                             Long subCategoryId, Long warehouseId, Long warehouseStoreId,
                                                                     Pageable pageable);

    interface PendingApprovalUserItem extends UserItem{
        String getEmployeeName();
        String getWarehouseName();
        String getStoreName();
    }

    interface UserItemByAttribute{
        Long getId();
        Long getBrandId();
        String getItemAttributes();
    }

    interface UserItem{
        Long getId();
        String getBrandName();
        String getCategoryName();
        String getSubCategoryName();
        String getProductName();
        String getStatus();

    }
}
