package com.agi.aesl.erpscm.store_receive.repository;

import com.agi.aesl.erpscm.store_receive.entity.SrnVerifyApprovalHistory;
import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SrnVerifyApprovalHistoryRepository extends JpaRepository<SrnVerifyApprovalHistory, Long> {
}
