package com.agi.aesl.erpscm.store_receive.repository;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveDetail;
import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveNote;
import com.agi.aesl.erpscm.store_receive.enums.SrnStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SrnRepository extends JpaRepository<StoreReceiveNote, Long>, SrnQuery {

    @Query(value = getAll, countQuery = countAll, nativeQuery = true)
    Page<StoreReceiveNoteInfo> findAllSrnByStatus(List<String> status, LocalDateTime fromDate,
                               LocalDateTime toDate, Pageable pageable);

    @Query(value = getPendingDemandsBySrnForSrnItems,nativeQuery = true)
    List<PendingDemandList> getPendingDemandsBySrnForSrnItems(@Param("id") Long id);

    @Query(value = getGetPendingDemandsByAttributes, nativeQuery = true)
    List<PendingDemandList> getPendingDemandsBySrnForSrnItems(@Param("attributes") String attributes);

    @Query(value = getPendingVerifications, countQuery = countPendingVerifications, nativeQuery = true)
    Page<StoreReceiveNoteInfo> findAllPendingVerification(String nextVerifierId, LocalDateTime fromDate,
                                                          LocalDateTime toDate, Pageable pageable);

    @Query(value = getPendingApprovals, countQuery = countPendingApprovals, nativeQuery = true)
    Page<StoreReceiveNoteInfo> findAllPendingApproval(String nextApproverId, LocalDateTime fromDate,
                                                      LocalDateTime toDate, Pageable pageable);

    <T> Optional<T> findById(Long id, Class<T> srnDetailClass);

    interface StoreReceiveNoteInfo{
        String getGrnNo();
        Long getId();
        Long getSrnId();
        String getGrnStatus();
        String getCategoryName();
        BigDecimal getReceivedQty();
        String getItemAttributes();
        Long getBrandId();
        Long getItems();
        Long getDemands();
        String getSrnStatus();
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDateTime getCreatedAt();
    }

    interface SrnDetail{
        Long getId();
        String getSrnNo();
        String getComment();
        String getNextVerifierId();
        String getNextApproverId();
        SrnStatus getSrnStatus();

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate getCreatedAt();
        List<StoreReceiveDetail> getSrnDetails();

        Employee getEmployee();
        SrnStatus getReviewPrevStatus();
        LocalDate getReviewDate();
    }
}
