package com.agi.aesl.erpscm.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.inventory.entity.ItemStock;

@Repository
public interface ItemStockRepository extends JpaRepository<ItemStock,Long>{
    
}
