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

import static com.agi.aesl.erpscm.indent.repository.IndentQuery.*;

@Repository
public interface IndentRepository extends JpaRepository<Indent,Long>{

    @Query(value = GET_ALL_INDENT, countQuery = COUNT_ALL_INDENT, nativeQuery = true)
    Page<IndentInfo> getAllIndents(List<Long> categoryIds, List<Long> warehouseIds,
                                   LocalDateTime fromDate, LocalDateTime toDate,
                                   String indentNo,
                                   Pageable pageable);

    @Query(value = GET_INDENT_PV, countQuery = COUNT_INDENT_PV, nativeQuery = true)
    Page<IndentInfo> getAllPendingVerifications(String nextVerifierId,
                                                List<Long> categoryIds,
                                                List<Long> warehouseIds,
                                                LocalDateTime fromDate,
                                                LocalDateTime toDate,
                                                String indentNo,
                                                Pageable pageable);

    @Query(value = GET_INDENT_PA, countQuery = COUNT_INDENT_PA, nativeQuery = true)
    Page<IndentInfo> getAllPendingApprovals(String nextApproverId,
                                                List<Long> categoryIds,
                                                List<Long> warehouseIds,
                                                LocalDateTime fromDate,
                                                LocalDateTime toDate,
                                                String indentNo,
                                                Pageable pageable);
    @Query(value = GET_CLOSED_INDENTS, countQuery = COUNT_ALL_CLOSED, nativeQuery = true)
    Page<IndentInfo> getAllClosedIndents(List<Long> categoryIds, List<Long> warehouseIds,
                                         String indentNo,Pageable pageable);

    @Query(value = GET_INDENT_DETAIL, nativeQuery = true)
    List<IndentViewInfo> getIndentById(Long id);

    Optional<Indent> findByRfqUuid(String code);

    @Query(value = GET_INDENT_DETAIL_WITH_ID_RANGE, nativeQuery = true)
    List<IndentViewInfo> getIndentByIds(List<Long> ids);

    @Modifying
    @Query(value = "UPDATE Indent i SET i.istatus = 'OPEN' WHERE i.id in (:ids)")
    int moveIndentByIds(List<Long> ids);

    @Query("SELECT MAX(i.id) FROM Indent i")
    Optional<Long> findMaxIndentById();

    @Query(value = GET_INDENT_APPROVED_AND_PENDING_RFQ_WITH_SEARCH, countQuery = COUNT_PENDING_RFQ, nativeQuery = true)
    Page<IndentInfo> getAllApprovedIndents(String indentNo, Long category, Long subCategory, String priority,
                                  Integer daysRemain, LocalDateTime fromDate, LocalDateTime toDate,
                                  Pageable pageable);

    @Query(value = GET_APPROVED_INDENT_WITH_OPEN_RFQ, countQuery = COUNT_APPROVED_INDENT_WITH_OPEN_RFQ, nativeQuery = true)
    Page<SentRfqListItem> getAllIndentsWithOpenRfqStatus(String indentNo, Long category, Long subCategory,
                                                         String priority, Integer daysRemain, LocalDateTime fromDate,
                                                         LocalDateTime toDate, Pageable pageable);


    @Query(value = GET_ALL_INDENT_BY_EXP_DATETIME_WITH_SEARCH,
            countQuery = COUNT_ALL_INDENT_BY_EXP_DATETIME_WITH_SEARCH,
            nativeQuery = true
    )
    Page<CsListInfo> getAllIndentsByExpireDateTime(@Param("expiredDateTime") LocalDateTime currentDateTime,
                                                   String indentNo,
                                                   List<String> status,
                                                   LocalDateTime fromDate,
                                                   LocalDateTime toDate,
                                                   Pageable pageable);

    @Query(value = GET_ALL_CLOSED_RFQ,
            countQuery = COUNT_ALL_CLOSED_RFQ,
            nativeQuery = true
    )
    Page<CsListInfo> getAllIndentsWithCloseRfqStatus(String indentNo, String category, String subCategory,
                                                     String priority, Integer daysRemain,
                                                     LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    interface SentRfqListItem extends IndentInfo {
        LocalDateTime getSentDate();
        Integer getReceivedQty();
        Integer getTotalReceivedPq();
        BigDecimal getRfqQty();
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
        BigDecimal getOrderQty();
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
        Long getReceivedQty();
        BigDecimal getRfqQty();
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
        BigDecimal getPdQty();
        String getIndentNo();
        Long getCategoryId();
        String getCategoryName();
        Long getSubCategoryId();

        String getProductRequirementIds();

        String getSubCategoryName();

        String getItemName();

        BigDecimal getOrderQty();
        BigDecimal getRfqQty();
        BigDecimal getPrQty();
        String getPriority();
        LocalDate getPriorityDate();
        Long getDaysRemain();
        Long getBrandId();
        String getBrandName();

    }
}
