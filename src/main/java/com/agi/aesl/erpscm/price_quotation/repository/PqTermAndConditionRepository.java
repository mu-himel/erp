package com.agi.aesl.erpscm.price_quotation.repository;

import com.agi.aesl.erpscm.price_quotation.entity.PqTermsAndCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PqTermAndConditionRepository extends JpaRepository<PqTermsAndCondition,Long> {
}
