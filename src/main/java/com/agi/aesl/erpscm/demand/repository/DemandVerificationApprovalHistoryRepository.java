package com.agi.aesl.erpscm.demand.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.demand.entity.DemandVerificationApprovalHistory;

@Repository
public interface DemandVerificationApprovalHistoryRepository extends JpaRepository<DemandVerificationApprovalHistory,Long> {

    
  
}
