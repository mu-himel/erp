package com.agi.aesl.erpscm.inventory.user_request.repository;

import com.agi.aesl.erpscm.inventory.user_request.entity.UserItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem,Long>, UserItemQuery {

    @Query(value = """
            SELECT * FROM (SELECT i.id, i.brand_id ,i.active,
                    GROUP_CONCAT(DISTINCT  ia.attribute_type,' ',ia.attribute_value , ' ',ia.attribute_unit ORDER BY ia.id ASC separator ' - ') itemAttributes
            FROM user_item_attributes ia
            LEFT JOIN user_items i on i.id=ia.user_item_id
            GROUP BY i.id) p
            WHERE p.brand_id=:brandId AND itemAttributes = :attribute
            """,nativeQuery = true)
    List<?> findByAttributes(Long brandId, String attribute);

    @Query(value = getMyList, countQuery = countMyList, nativeQuery = true)
    Page<UserItem> findAllByCreatedById(String userId,Long categoryId,
                                        Long subCategoryId,Pageable pageable);

    @Query(value = getPendingVerifications, countQuery = countPendingVerifications,nativeQuery = true)
    Page<UserItem> findAllPendingVerifications(String userId,Long categoryId,
                                               Long subCategoryId,
                                               Pageable pageable);

    @Query(value = getPendingApprovals, countQuery =  countPendingApprovals, nativeQuery = true)
    Page<UserItem> findAllPendingApprovals(String userId, Long categoryId,
                                           Long subCategoryId,Pageable pageable);

    @Query(value = getClosed, countQuery =  countClosed, nativeQuery = true)
    Page<UserItem> findAllClosed(String userId, Long categoryId,
                                 Long subCategoryId,Pageable pageable);

    @Query(value = getPendingApprovalsByStore, countQuery =  countPendingApprovalsByStore, nativeQuery = true)
    Page<PendingApprovalUserItem> findAllPendingApprovalItemsByStore(String userId, Long categoryId, Long subCategoryId, Pageable pageable);

    interface PendingApprovalUserItem extends UserItem{
        String getEmployeeName();
    }

    interface UserItem{
        Long getId();
        String getCategoryName();
        String getSubCategoryName();
        String getProductName();
        String getStatus();

    }
}
