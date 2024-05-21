package com.agi.aesl.erpscm.inventory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;

@Repository
public interface CategoryBrandRepository extends JpaRepository<CategoryBrand,Long>{

    Optional<CategoryBrand> findByCategoryIdAndName(Long id, String brands);
    
}
