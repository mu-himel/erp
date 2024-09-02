package com.agi.aesl.erpscm.store_receive.repository;

import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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

    interface StoreReceiveNoteInfo{
        String getGrnNo();
        Long getSrnId();
        String getGrnStatus();
        String getCategoryName();
        Long getDemands();
        String getSrnStatus();
    }
}
