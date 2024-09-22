package com.agi.aesl.erpscm.purchase_order.repository;

import com.agi.aesl.erpscm.purchase_order.entity.PoGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PoGroupRepository extends JpaRepository<PoGroup,Long> {
    Optional<PoGroup> findByCsId(Long csId);
}
