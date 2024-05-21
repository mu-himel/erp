package com.agi.aesl.erpscm.inventory.repository;

import com.agi.aesl.erpscm.inventory.entity.CategoryBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget,Long> {
}
