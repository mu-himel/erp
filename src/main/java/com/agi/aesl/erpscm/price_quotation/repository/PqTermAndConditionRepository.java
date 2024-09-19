package com.agi.aesl.erpscm.price_quotation.repository;

import com.agi.aesl.erpscm.price_quotation.entity.PqTermsAndCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PqTermAndConditionRepository extends JpaRepository<PqTermsAndCondition,Long> {
    @Query(value = """
            SELECT DISTINCT tnc FROM PqTermsAndCondition tnc
            WHERE tnc.rfq.id = :rfqId AND tnc.vendorId=:vendorId
            GROUP BY tnc.termAndCondition
            """)
    List<PqTermsAndCondition> findAllByRfqIdAndVendorId(@Param("rfqId")Long tenderId, @Param("vendorId") Long vendorId);
}
