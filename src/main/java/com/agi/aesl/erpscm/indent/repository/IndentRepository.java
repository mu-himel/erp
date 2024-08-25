package com.agi.aesl.erpscm.indent.repository;

import com.agi.aesl.erpscm.indent.entity.Indent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndentRepository extends JpaRepository<Indent,Long> {
}
