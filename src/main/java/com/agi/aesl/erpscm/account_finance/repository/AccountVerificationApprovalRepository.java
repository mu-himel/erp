package com.agi.aesl.erpscm.account_finance.repository;

import com.agi.aesl.erpscm.account_finance.entity.LedgerAccountVerifyApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountVerificationApprovalRepository extends JpaRepository<LedgerAccountVerifyApprovalHistory,Long> {
    void deleteAllByLedgerAccountId(Long id);
}
