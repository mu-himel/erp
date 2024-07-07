package com.agi.aesl.erpscm.product_requirements.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.product_requirements.entity.ProductRequirement;

@Repository
public interface ProductRequirementRepository extends JpaRepository<ProductRequirement,Long>{
    
}
