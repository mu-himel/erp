package com.agi.aesl.erpscm.inventory.repository;

import com.agi.aesl.erpscm.inventory.entity.CategoryBudget;
import com.agi.aesl.erpscm.inventory.enums.BudgetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget,Long> {


    List<CategoryBudget> findByCategoryIdAndBudgetTypeAndCurrentYear(Long id, BudgetType budgetType, Integer currentYear);
}
