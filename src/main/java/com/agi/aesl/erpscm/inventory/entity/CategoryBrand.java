package com.agi.aesl.erpscm.inventory.entity;

import com.agi.aesl.erpscm.common.BrandInterface;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "scm_category_brands")
public class CategoryBrand implements BrandInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JsonIgnore
    private ItemCategory category;

    private Boolean isCustom;

    public CategoryBrand(Long id) {
        this.id = id;
    }

    public CategoryBrand(String name){
        this.name = name;
    }
}
