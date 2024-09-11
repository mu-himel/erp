package com.agi.aesl.erpscm.inventory.user_request.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_category_attributes")
public class UserCategoryAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
