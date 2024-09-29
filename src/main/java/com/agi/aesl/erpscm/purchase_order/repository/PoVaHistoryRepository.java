package com.agi.aesl.erpscm.purchase_order.repository;

import com.agi.aesl.erpscm.purchase_order.entity.PoVerificationApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoVaHistoryRepository extends JpaRepository<PoVerificationApprovalHistory,Long> {
}
