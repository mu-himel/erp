package com.agi.aesl.erpscm.store_receive.repository;

import com.agi.aesl.erpscm.store_receive.entity.SrnVerifyApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SrnVerifyApprovalHistoryRepository extends JpaRepository<SrnVerifyApprovalHistory, Long> {
}
