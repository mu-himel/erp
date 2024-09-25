package com.agi.aesl.erpscm.inventory.repository;

import com.agi.aesl.erpscm.inventory.entity.ItemFunctionalUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemFuncationalUnitRepository extends JpaRepository<ItemFunctionalUnit,Long> {
    List<ItemFunctionalUnit> findAllByItemId(Long id);
}
