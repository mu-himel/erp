package com.agi.aesl.erpscm.indent.repository;

import com.agi.aesl.erpscm.indent.entity.IndentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndentDetailRepository extends JpaRepository<IndentDetail,Long> {
}
