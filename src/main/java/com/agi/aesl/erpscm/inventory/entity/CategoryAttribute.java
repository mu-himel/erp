package com.agi.aesl.erpscm.inventory.entity;

import com.agi.aesl.erpscm.inventory.enums.AttributeUnit;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;



@Entity
@Data
@Table(name = "scm_category_attributes")
public class CategoryAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String attributeType;

    
    private String attributeUnit;

    @Column(length = 500)
    private String attributeValue;

    @ManyToOne
    @JsonIgnore
    private ItemCategory category;
}
