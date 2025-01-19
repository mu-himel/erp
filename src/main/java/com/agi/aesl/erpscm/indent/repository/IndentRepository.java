package com.agi.aesl.erpscm.indent.repository;

import com.agi.aesl.erpscm.indent.entity.Indent;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IndentRepository extends JpaRepository<Indent,Long>, IndentQuery{

    @Query(value = getAllIndents, countQuery = countAllIndents, nativeQuery = true)
    Page<IndentInfo> getAllIndents(List<Long> categoryIds, List<Long> warehouseIds,
                                   LocalDateTime fromDate, LocalDateTime toDate,
                                   Pageable pageable);

    @Query(value = getIndentPendingVerifications, countQuery = countAllPendingVerifications, nativeQuery = true)
    Page<IndentInfo> getAllPendingVerifications(String nextVerifierId,
                                                List<Long> categoryIds,
                                                List<Long> warehouseIds,
                                                LocalDateTime fromDate,
                                                LocalDateTime toDate,
                                                Pageable pageable);

    @Query(value = getIndentPendingApprovals, countQuery = countAllPendingVerifications, nativeQuery = true)
    Page<IndentInfo> getAllPendingApprovals(String nextApproverId,
                                                List<Long> categoryIds,
                                                List<Long> warehouseIds,
                                                LocalDateTime fromDate,
                                                LocalDateTime toDate,
                                                Pageable pageable);
    @Query(value = getClosedIndents, countQuery = countAllClosed, nativeQuery = true)
    Page<IndentInfo> getAllClosedIndents(List<Long> categoryIds, List<Long> warehouseIds, Pageable pageable);

    @Query(value = getIndentDetail, nativeQuery = true)
    List<IndentViewInfo> getIndentById(Long id);

    Optional<Indent> findByRfqUuid(String code);

    @Query(value = getIndentDetailWithIdRange, nativeQuery = true)
    List<IndentViewInfo> getIndentByIds(List<Long> ids);

    @Modifying
    @Query(value = "UPDATE Indent i SET i.istatus = 'OPEN' WHERE i.id in (:ids)")
    int moveIndentByIds(List<Long> ids);

    @Query("SELECT MAX(i.id) FROM Indent i")
    Optional<Long> findMaxIndentById();

    @Query(value = getIndentApprovedAndPendingRFqWithSearch, countQuery = countPendingRfqs, nativeQuery = true)
    Page<IndentInfo> getAllApprovedIndents(String indentNo, String category, String subCategory, String priority,
                                  Integer daysRemain, LocalDateTime fromDate, LocalDateTime toDate,
                                  Pageable pageable);

    @Query(value = getApprovedIndentWithOpenRfq, countQuery = countApprovedIndentWithOpenRfq, nativeQuery = true)
    Page<SentRfqListItem> getAllIndentsWithOpenRfqStatus(String indentNo, String category, String subCategory,
                                                         String priority, Integer daysRemain, LocalDateTime fromDate,
                                                         LocalDateTime toDate, Pageable pageable);


    @Query(value = getAllIndentsByExpireDateTimeWithSearch,
            countQuery = countAllIndentsByExpireDateTimeWithSearch,
            nativeQuery = true
    )
    Page<CsListInfo> getAllIndentsByExpireDateTime(@Param("expiredDateTime") LocalDateTime currentDateTime,
                                                   String indentNo,
                                                   List<String> status,
                                                   LocalDateTime fromDate,
                                                   LocalDateTime toDate,
                                                   Pageable pageable);

    @Query(value = getAllClosedRfq,
            countQuery = countAllClosedRfq,
            nativeQuery = true
    )
    Page<CsListInfo> getAllIndentsWithCloseRfqStatus(String indentNo, String category, String subCategory,
                                                     String priority, Integer daysRemain,
                                                     LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    interface SentRfqListItem extends IndentInfo {
        LocalDateTime getSentDate();
        Integer getReceivedQty();
        Integer getTotalReceivedPq();
        Long getRfqQty();
    }
    interface IndentInfo {
        Long getId();
        String getIndentNo();
        Long getCategoryId();
        String getCategoryName();
        String getSubCategoryIds();
        String getSubCategoryNames();
        String getSubCategoryName();
        Long getItemsCount();
        Long getOrderQty();
        String getPriority();
        Long getDaysRemain();
        String getStatus();
        String getIndentStatus();
        String getEmployeeName();
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate getIndentDate();
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDateTime getSentDate();
    }

    interface CsListInfo {
        Long getId();
        Long getCsId();
        String getIndentNo();
        String getCategoryName();
        String getSubCategoryName();
        Long getItemsCount();
        Long getRfqQty();
        String getStatus();
        Long getLockedVendor();
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate getIndentDate();
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate getSentDate();
    }



    interface IndentViewInfo{
        Long getId();
        Long getDetailId();
        Long getWarehouseId();
        String getWarehouseName();
        Long getPiwId();
        Long getPdId();
        LocalDate getPdDate();
        Long getPdQty();
        String getIndentNo();
        Long getCategoryId();
        String getCategoryName();
        Long getSubCategoryId();

        String getProductRequirementIds();

        String getSubCategoryName();

        String getItemName();

        BigDecimal getOrderQty();
        Long getRfqQty();
        Long getPrQty();
        String getPriority();
        LocalDate getPriorityDate();
        Long getDaysRemain();
        Long getBrandId();
        String getBrandName();

    }
}
