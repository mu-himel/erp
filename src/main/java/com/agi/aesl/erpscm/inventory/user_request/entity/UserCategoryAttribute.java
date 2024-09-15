package com.agi.aesl.erpscm.inventory.user_request.entity;

import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_category_attributes")
public class UserCategoryAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String attributeType;


    private String attributeUnit;

    @Column(length = 500)
    private String attributeValue;

    @ManyToOne
    @JsonIgnore
    private UserCategory category;

    public UserCategoryAttribute(CategoryAttribute ca, UserCategory userCategory) {
        this.attributeType = ca.getAttributeType();
        this.attributeUnit = ca.getAttributeUnit();
        this.attributeValue = ca.getAttributeValue();
        this.setCategory(userCategory);
    }
}
