package com.agi.aesl.erpscm.cs.repository;

import com.agi.aesl.erpscm.cs.entity.CsDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsDetailRepository extends JpaRepository<CsDetail,Long> {
}
