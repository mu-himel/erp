package com.agi.aesl.erpscm.indent.repository;

import com.agi.aesl.erpscm.indent.entity.IndentVerificationApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndentVerificationApprovalRepository extends JpaRepository<IndentVerificationApprovalHistory,Long> {
    void deleteAllByIndentId(Long id);
}
