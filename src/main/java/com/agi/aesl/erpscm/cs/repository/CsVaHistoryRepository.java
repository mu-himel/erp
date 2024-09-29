package com.agi.aesl.erpscm.cs.repository;

import com.agi.aesl.erpscm.cs.entity.CsVerificationApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CsVaHistoryRepository extends JpaRepository<CsVerificationApprovalHistory,Long> {
    void deleteAllByCsId(Long id);

    @Modifying
    @Query(value = """
            DELETE FROM cs_verification_approval_histories csvah WHERE csvah.cs_id=:csId
            """,nativeQuery = true)
    void removeByCsId(@Param("csId") Long csId);
}
