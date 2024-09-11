package com.agi.aesl.erpscm.inventory.user_request.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_items")
public class UserItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
