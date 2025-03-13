package com.agi.aesl.erpscm.cs.repository;

import com.agi.aesl.erpscm.cs.entity.CsAccountVAHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsAccountVAHistoryRepository extends JpaRepository<CsAccountVAHistory,Long> {
    void deleteByCsAccountId(Long id);
}
