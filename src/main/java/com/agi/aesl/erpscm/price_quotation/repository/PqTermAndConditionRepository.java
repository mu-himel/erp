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

    List<PqTermsAndCondition> findAllByVendorIdAndPriceQuotationId(Long vendorId, Long priceQuotationId);

    @Query(value = """
            SELECT pqts FROM PqTermsAndCondition pqts
            LEFT JOIN pqts.priceQuotation pq
            WHERE pqts.vendorId=:vendorId AND pq.id IN (:ids)
            """)
    List<PqTermsAndCondition> findAllByVendorIdAndPriceQuotationId(Long vendorId, List<Long> ids);
}
