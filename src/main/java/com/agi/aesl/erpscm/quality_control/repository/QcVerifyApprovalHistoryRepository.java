package com.agi.aesl.erpscm.quality_control.repository;

import com.agi.aesl.erpscm.quality_control.entity.QcVerifyApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QcVerifyApprovalHistoryRepository extends JpaRepository<QcVerifyApprovalHistory,Long> {

}
