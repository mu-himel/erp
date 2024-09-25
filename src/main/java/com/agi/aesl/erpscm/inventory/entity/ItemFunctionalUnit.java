package com.agi.aesl.erpscm.inventory.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name="scm_item_functional_units")
@Data
@NoArgsConstructor
public class ItemFunctionalUnit implements FunctionalUnitInterface{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal value;

    private String unit;

    @ManyToOne
    @JsonIgnore
    private Item item;
}
