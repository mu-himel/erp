package com.agi.aesl.erpscm.inventory.entity;


import com.agi.aesl.erpscm.inventory.user_request.entity.UserItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name="user_item_functional_units")
@Data
@NoArgsConstructor
public class UserItemFunctionalUnit implements FunctionalUnitInterface{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal value;

    private String unit;

    @ManyToOne
    @JsonIgnore
    private UserItem userItem;

    public UserItemFunctionalUnit(UserItemFunctionalUnit unit, UserItem userItem) {
        this.value = unit.getValue();
        this.unit = unit.getUnit();
        this.setUserItem(userItem);
    }
}
