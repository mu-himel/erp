package com.agi.aesl.erpscm.pr_indent.repository;


import com.agi.aesl.erpscm.pr_indent.entity.PrIndentPartialDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrIndentPartialDeliveryRepository extends JpaRepository<PrIndentPartialDelivery,Long>{
    
}
