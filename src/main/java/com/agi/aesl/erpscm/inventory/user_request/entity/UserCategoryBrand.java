package com.agi.aesl.erpscm.inventory.user_request.entity;

import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_category_brands")
public class UserCategoryBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JsonIgnore
    private UserCategory category;

    public UserCategoryBrand(Long id) {
        this.id = id;
    }

    public UserCategoryBrand(String name){
        this.name = name;
    }

    public UserCategoryBrand(String cb, UserCategory userCategory) {
        this.name = cb;
        this.setCategory(userCategory);
    }
}
