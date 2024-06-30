package com.agi.aesl.erpscm.demand.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.demand.entity.DemandDetailAttribute;

@Repository
public interface DemandDetailAttributeRepository extends JpaRepository<DemandDetailAttribute,Long>{
    List<DemandDetailAttribute> findByDemandDetailId(Long id);
    
}
