package com.agi.aesl.erpscm.indent.repository;

import com.agi.aesl.erpscm.indent.entity.Indent;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IndentRepository extends JpaRepository<Indent,Long>, IndentQuery{

    @Query(value = getAllIndents, countQuery = countAllIndents, nativeQuery = true)
    Page<IndentInfo> getAllIndents(List<Long> categoryIds, List<Long> warehouseIds,Pageable pageable);

    @Query(value = getIndentPendingVerifications, countQuery = countAllPendingVerifications, nativeQuery = true)
    Page<IndentInfo> getAllPendingVerifications(String nextVerifierId,
                                                List<Long> categoryIds,
                                                List<Long> warehouseIds,
                                                Pageable pageable);

    @Query(value = getIndentPendingApprovals, countQuery = countAllPendingVerifications, nativeQuery = true)
    Page<IndentInfo> getAllPendingApprovals(String nextApproverId,
                                                List<Long> categoryIds,
                                                List<Long> warehouseIds,
                                                Pageable pageable);

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
}
