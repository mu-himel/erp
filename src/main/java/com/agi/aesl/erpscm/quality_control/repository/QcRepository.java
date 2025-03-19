package com.agi.aesl.erpscm.quality_control.repository;

import com.agi.aesl.erpscm.goods_receive.repository.GrnRepository;
import com.agi.aesl.erpscm.quality_control.entity.QualityControl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QcRepository extends JpaRepository<QualityControl,Long>,QcQuery {

    @Query(value = QC_RESULT_BY_GRN_ID,nativeQuery = true)
    List<QcResultItem> getQcResultByGrn(Long id);

    @Query(value = GET_PENDING_VERIFICATIONS_QC,countQuery = COUNT_PENDING_VERIFICATIONS_QC,nativeQuery = true)
    Page<GrnRepository.GoodReceiveNoteInfo> findAllPendingVerification(List<Long> warehouseIds, List<Long> categoryIds,
                                                                       String nextVerifierId, List<String> status,
                                                                       String grnNo, Integer qty, Integer receivedQty,
                                                                       LocalDateTime fromDate, LocalDateTime toDate,
                                                                       Pageable pageable);

    @Query(value = GET_PENDING_APPROVALS_QC,countQuery = COUNT_PENDING_APPROVALS_QC,nativeQuery = true)
    Page<GrnRepository.GoodReceiveNoteInfo> findAllPendingApproval(List<Long> warehouseIds, List<Long> categoryIds,
                                                                   String nextApproveId, String grnNo,
                                   Integer qty, Integer receivedQty,List<String> status,
                                                                   LocalDateTime fromDate,
                                   LocalDateTime toDate, Pageable pageable);

    @Query(value = GET_CLOSED_QC,countQuery = COUNT_CLOSED,nativeQuery = true)
    Page<GrnRepository.GoodReceiveNoteInfo> findAllClosed(List<Long> warehouseIds, List<Long> categoryIds,String grnNo,
                                   Integer qty, Integer receivedQty, LocalDateTime fromDate,
                                   LocalDateTime toDate, Pageable pageable);

    @Query(value = GET_REJECTED_QC,countQuery = COUNT_REJECTED_QC,nativeQuery = true)
    Page<GrnRepository.GoodReceiveNoteInfo> findAllRejected(List<Long> warehouseIds, List<Long> categoryIds, String grnNo,
                          Integer qty, Integer receivedQty, LocalDateTime fromDate,
                          LocalDateTime toDate, Pageable pageable);
}
