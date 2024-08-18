package com.agi.aesl.erpscm.pr_indent.repository;


import com.agi.aesl.erpscm.pr_indent.entity.PrIndentWarehouseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrIndentWarehouseDetailRepository extends JpaRepository<PrIndentWarehouseDetail,Long>{
    
}
