package com.agi.aesl.erpscm.inventory.entity;

import com.agi.aesl.erpscm.inventory.enums.BudgetType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;

@Data
@Entity
@NoArgsConstructor
@Table(name = "scm_category_budgets")
public class CategoryBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private ItemCategory category;

    private BigDecimal amount;

    private Integer currentYear;

    @Enumerated(EnumType.STRING)
    private BudgetType budgetType;

    public CategoryBudget(ItemCategory category, BigDecimal amount, Integer currentYear, BudgetType budgetType) {
        this.category = category;
        this.amount = amount;
        this.currentYear = currentYear;
        this.budgetType = budgetType;
    }
}
