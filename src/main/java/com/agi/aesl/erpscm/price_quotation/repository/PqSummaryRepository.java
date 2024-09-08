package com.agi.aesl.erpscm.price_quotation.repository;

import com.agi.aesl.erpscm.price_quotation.entity.PqTermsAndCondition;
import com.agi.aesl.erpscm.price_quotation.entity.PriceQuotationSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PqSummaryRepository extends JpaRepository<PriceQuotationSummary,Long> {
}
