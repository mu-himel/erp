package com.agi.aesl.erpscm.internal_requisition.repository;

import com.agi.aesl.erpscm.internal_requisition.entity.IrVAHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IrVAHistoryRepository extends JpaRepository<IrVAHistory,Long> {
}
