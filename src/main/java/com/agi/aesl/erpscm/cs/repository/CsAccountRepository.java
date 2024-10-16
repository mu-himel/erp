package com.agi.aesl.erpscm.cs.repository;

import com.agi.aesl.erpscm.cs.entity.CsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsAccountRepository extends JpaRepository<CsAccount,Long> {
}
